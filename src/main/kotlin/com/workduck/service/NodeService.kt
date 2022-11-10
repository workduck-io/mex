package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.serverless.models.requests.BlockMovementRequest
import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodeNamePath
import com.serverless.models.requests.NodePath
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.RefactorRequest
import com.serverless.models.requests.SharedNodeRequest
import com.serverless.models.requests.UpdateAccessTypesRequest
import com.serverless.models.requests.UpdateSharedNodeRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.addAlphanumericStringToTitle
import com.serverless.utils.addIfNotEmpty
import com.serverless.utils.awaitAndThrowExceptionIfFalse
import com.serverless.utils.commonPrefixList
import com.serverless.utils.convertToPathString
import com.serverless.utils.createNodePath
import com.serverless.utils.getDifferenceWithOldHierarchy
import com.serverless.utils.getListOfNodes
import com.serverless.utils.getRoughSizeOfEntity
import com.serverless.utils.isNodeUnchanged
import com.serverless.utils.mix
import com.serverless.utils.removePrefixList
import com.workduck.models.AccessType
import com.workduck.models.AdvancedElement
import com.workduck.models.Entity
import com.workduck.models.EntityOperationType
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.MatchType
import com.workduck.models.Namespace
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.NodeAccess
import com.workduck.models.NodeIdentifier
import com.workduck.models.NodeVersion
import com.workduck.models.Page
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.models.exceptions.WDNodeSizeLargeException
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.PageRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.cache.NodeCache
import com.workduck.utils.AccessItemHelper
import com.workduck.utils.AccessItemHelper.getNodeAccessItems
import com.workduck.utils.AccessItemHelper.getNodeAccessItemsFromAccessMap
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.NodeHelper
import com.workduck.utils.NodeHelper.getIDPath
import com.workduck.utils.NodeHelper.getNamePath
import com.workduck.utils.NodeHelper.getNodeIDsFromHierarchy
import com.workduck.utils.NodeHelper.updateNodePath
import com.workduck.utils.PageHelper.createDataOrderForPage
import com.workduck.utils.PageHelper.mergePageVersions
import com.workduck.utils.PageHelper.orderBlocks
import com.workduck.utils.RelationshipHelper.findStartNodeOfEndNode
import com.workduck.utils.TagHelper.createTags
import com.workduck.utils.TagHelper.deleteTags
import com.workduck.utils.TagHelper.updateTags
import com.workduck.utils.WorkspaceHelper.removeRedundantPaths
import com.workduck.utils.extensions.toInt
import com.workduck.utils.extensions.toNode
import com.workduck.utils.extensions.toNodeIDList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.apache.logging.log4j.LogManager

/**
 * contains all node related logic
 */
class NodeService( // Todo: Inject them from handlers
    val objectMapper: ObjectMapper = Helper.objectMapper,
    val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    val dynamoDB: DynamoDB = DynamoDB(client),
    val mapper: DynamoDBMapper = DynamoDBMapper(client),

    var tableName: String = DDBHelper.getTableName(),

    var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val pageRepository: PageRepository<Node> = PageRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),

    private val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig),

) {
    private val workspaceService: WorkspaceService = WorkspaceService(nodeService = this)
    private val namespaceService: NamespaceService = NamespaceService(nodeService = this)
    private val nodeAccessService: NodeAccessService = NodeAccessService(nodeRepository, namespaceService.namespaceAccessService)

    fun deleteBlockFromNode(blockIDRequest: WDRequest, workspaceID: String, nodeID: String, userID: String) {
        val blockIDList = (blockIDRequest as GenericListRequest).toNodeIDList()
        nodeRepository.getNodeDataOrderByNodeID(nodeID, workspaceID).let {
            nodeRepository.deleteBlockAndDataOrderFromNode(blockIDList, workspaceID, nodeID, userID, it)
        }
    }

    fun createNode(node: Node, namespace: Namespace) : Node = runBlocking {
        setMetadataOfNodeToCreate(node, namespace)

        launch { createTags(node.tags, node.id, node.workspaceIdentifier.id) }
        repository.create(node)
        return@runBlocking node

    }



    /* if operation is "create", will be used to create just a single leaf node */
    fun createAndUpdateNode(request: WDRequest?, userWorkspaceID: String, userID: String): Node =
        runBlocking {
            val nodeRequest: NodeRequest = request as NodeRequest

            val nodeWorkspaceID = nodeAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(nodeRequest.id, userWorkspaceID,
                    nodeRequest.namespaceIdentifier.id, userID, EntityOperationType.WRITE).let { workspaceDetails ->
                        require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                        workspaceDetails[Constants.WORKSPACE_ID]!!
            }


            val node: Node = createNodeObjectFromNodeRequest(nodeRequest, nodeWorkspaceID, userID)

            val jobToGetStoredNode = async { getNodeAfterPermissionCheck(node.id, userID, ItemStatus.ACTIVE) }

            val jobToGetNamespace = async {
                node.namespaceIdentifier.id.let { namespaceID ->
                    namespaceService.getNamespaceAfterPermissionCheck(namespaceID).let { namespace ->
                        require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
                        namespace
                    }
                }
            }


            return@runBlocking when (val storedNode = jobToGetStoredNode.await()) {
                null -> {
                    val namespace = jobToGetNamespace.await()
                    updateNodeAttributesInSingleCreate(node, nodeRequest, namespace)
                    createNode(node, namespace)
                }
                else -> {
                    jobToGetNamespace.cancel()
                    updateNode(node, storedNode)
                }
            }
        }



    fun createAndUpdateNodeV2(request: WDRequest?, userWorkspaceID: String, userID: String) =
            runBlocking {
                val nodeRequest: NodeRequest = request as NodeRequest

                val nodeWorkspaceID = nodeAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(nodeRequest.id, userWorkspaceID,
                        nodeRequest.namespaceIdentifier.id, userID, EntityOperationType.WRITE).let { workspaceDetails ->
                    require(!workspaceDetails[Constants.WORKSPACE_ID].isNullOrEmpty()) { Messages.ERROR_NODE_PERMISSION }
                    workspaceDetails[Constants.WORKSPACE_ID]!!
                }


                val node: Node = createNodeObjectFromNodeRequest(nodeRequest, nodeWorkspaceID, userID)

                val jobToGetStoredNode = async { getNodeAfterPermissionCheck(node.id, userID, ItemStatus.ACTIVE) }

                val jobToGetNamespace = async {
                    node.namespaceIdentifier.id.let { namespaceID ->
                        namespaceService.getNamespaceAfterPermissionCheck(namespaceID).let { namespace ->
                            require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
                            namespace
                        }
                    }
                }


                when (val storedNode = jobToGetStoredNode.await()) {
                    null -> {
                        val namespace = jobToGetNamespace.await()
                        updateNodeAttributesInSingleCreate(node, nodeRequest, namespace)
                        createNode(node, namespace)
                    }
                    else -> {
                        jobToGetNamespace.cancel()
                        updateNode(node, storedNode)
                    }
                }
            }

    fun updateNodeAttributesInSingleCreate(node: Node, nodeRequest: NodeRequest, namespace: Namespace) {
        node.title =
                updateNamespaceHierarchyInSingleCreateAndReturnTitle(
                        nodeRequest.referenceID,
                        node.id,
                        node.title,
                        node.workspaceIdentifier.id,
                        namespace
                )
    }

    private fun checkForPathClashAndResolveWithNewTitle(nodeHierarchy: List<String>, prefixNodePath: String, passedNodeTitle: String): String {

        // get the node name path after the node would get created
        val nodeNamePathToAdd = getNamePath(prefixNodePath).createNodePath(passedNodeTitle)

        // if the same name path already exists in the current hierarchy, update the passed node title
        return nodeHierarchy.lastOrNull {
            getNamePath(it) == nodeNamePathToAdd
        }?.let {
            nodeNamePathToAdd.getListOfNodes().last().addAlphanumericStringToTitle()
        } ?: passedNodeTitle
    }

    private fun updateNamespaceHierarchyInSingleCreateAndReturnTitle(
        referenceID: String?,
        nodeID: String,
        passedNodeTitle: String,
        workspaceID: String,
        namespace: Namespace
    ): String {

        NodeHelper.checkForDuplicateNodeID(namespace.nodeHierarchyInformation, nodeID)

        var nodeTitle = passedNodeTitle
        when (referenceID) {
            null -> { /* just a single standalone node is created */
                nodeTitle = checkForPathClashAndResolveWithNewTitle(namespace.nodeHierarchyInformation, "", passedNodeTitle)
                namespaceService.addPathToHierarchy(workspaceID, namespace.id, nodeTitle.createNodePath(nodeID))
                return nodeTitle
            }
            else -> { /* need to create the given node as child node */
                val nodeHierarchy = namespace.nodeHierarchyInformation.toMutableList()
                val nodePathsToAdd = mutableListOf<String>()
                val nodePathsToRemove = mutableListOf<String>()

                for (nodePath in nodeHierarchy) {
                    if (nodePath.endsWith(referenceID)) { /* referenceID was a leaf node */
                        nodeTitle = checkForPathClashAndResolveWithNewTitle(nodeHierarchy, nodePath, passedNodeTitle)
                        nodePathsToAdd.add("$nodePath${Constants.DELIMITER}$nodeTitle${Constants.DELIMITER}$nodeID")

                        if (!NodeHelper.isPathClashing(nodeTitle, passedNodeTitle)) nodePathsToRemove.add(nodePath)
                        break
                    } else if (nodePath.contains(referenceID)) { /* referenceID is not leaf node, and we need to extract path till referenceID */
                        val endIndex = nodePath.indexOf(referenceID) + referenceID.length - 1
                        val pathTillRefID = nodePath.substring(0, endIndex + 1)
                        nodeTitle = checkForPathClashAndResolveWithNewTitle(nodeHierarchy, pathTillRefID, passedNodeTitle)

                        nodePathsToAdd.add("$pathTillRefID${Constants.DELIMITER}$nodeTitle${Constants.DELIMITER}$nodeID")
                        break
                    } else {
                        continue
                    }
                }

                for (pathToRemove in nodePathsToRemove) {
                    nodeHierarchy.remove(pathToRemove)
                }

                for (pathToAdd in nodePathsToAdd) {
                    nodeHierarchy.add(pathToAdd)
                }
                namespaceService.updateNamespaceHierarchy(namespace, nodeHierarchy, HierarchyUpdateSource.NODE)
            }
        }
        return nodeTitle
    }

    /* break new node path in 4 parts and combine later
        1. Unchanged Prefix Path ( if any ) - longestExistingPath
        2. Path due to new Nodes in between ( if any ) - pathForNewNodes
        3. Path due to renaming of one node ( if any ) - renameNodePath
        ** 1,2,3 will remain unchanged. Combine first 3 and append each possibility of 4th to it.
        4. Unchanged Suffix Path ( if any )
    */

    fun refactor(wdRequest: WDRequest, userID: String, userWorkspaceID: String): Map<String, Any> = runBlocking {
        val refactorNodePathRequest = wdRequest as RefactorRequest

        /* existingNodePath is path from root till last node in the path and not necessarily path till a leaf node */
        val lastNodeID = refactorNodePathRequest.nodeID

        val existingNodes = refactorNodePathRequest.existingNodePath
        val newNodes = refactorNodePathRequest.newNodePath

        val workspaceID = nodeAccessService.checkAccessForRefactorAndGetWorkspaceID(userWorkspaceID, existingNodes.namespaceID, newNodes.namespaceID, lastNodeID, userID)

        // TODO(crosscheck if the last node name in the path is for the passed nodeID)
        val jobToGetExistingNamespace = async {
            existingNodes.namespaceID.let { namespaceID ->
                namespaceService.getNamespaceAfterPermissionCheck(namespaceID).let { namespace ->
                    require(namespace != null) { "Invalid NamespaceID ${existingNodes.namespaceID}" }
                    namespace
                }
            }
        }

        /* if the namespace id is same ( or null ), we are dealing with a single namespace ( or workspace ) */
        val jobToGetTargetNamespace = when (existingNodes.namespaceID == newNodes.namespaceID) {
            false -> async {
                newNodes.namespaceID.let { namespaceID ->
                    namespaceService.getNamespaceAfterPermissionCheck(namespaceID).let { namespace ->
                        require(namespace != null) { "Invalid NamespaceID ${newNodes.namespaceID}" }
                        namespace
                    }
                }
            }
            true -> jobToGetExistingNamespace
        }

        val paths = mutableListOf<String>()

        if (newNodes.allNodes.size > 1) addPathsAndCreateNodesBeforeLastNode(refactorNodePathRequest, paths, userID, jobToGetTargetNamespace.await(), workspaceID)

        launch { changeLastPassedNodeInRefactor(existingNodes, newNodes, lastNodeID, userID, workspaceID, jobToGetTargetNamespace.await()) }

        val renameNodePath = "${newNodes.allNodes.last()}${Constants.DELIMITER}$lastNodeID" /* even if no need to rename, we're good */
        LOG.debug("renameString : $renameNodePath")

        paths.addIfNotEmpty(renameNodePath)

        val combinedPath = paths.joinToString(Constants.DELIMITER)

        /* get paths emanating from lastNode */
        val lastNodeHierarchy = getHierarchyOfNode(jobToGetExistingNamespace.await().nodeHierarchyInformation, lastNodeID)

        launch { updateLastNodeHierarchyNodes(lastNodeHierarchy, workspaceID, jobToGetExistingNamespace.await(), jobToGetTargetNamespace.await()) }

        return@runBlocking updateHierarchyInRefactorAndReturnDifference(jobToGetExistingNamespace.await(), jobToGetTargetNamespace.await(),
                lastNodeHierarchy, combinedPath, existingNodes, workspaceID)
    }

    private fun updateLastNodeHierarchyNodes(lastNodeHierarchy: List<String>, workspaceID: String, sourceNamespace: Namespace, targetNamespace: Namespace) {
        when(sourceNamespace.id == targetNamespace.id) {
            true -> return /* no need to update public access field or namespace for the nodes */
            false -> {
                /* update namespace for the nodes in lastNodeHierarchy and set public access value of targetNamespace */
                updateNamespaceAndPublicAccessForSuccessorNodes(targetNamespace.id, workspaceID, lastNodeHierarchy, targetNamespace.publicAccess.toInt())
            }

        }
    }


    private fun changeLastPassedNodeInRefactor(
        existingNodes:  NodeNamePath,
        newNodes:  NodeNamePath,
        lastNodeID: String,
        userID: String,
        workspaceID: String,
        targetNamespace: Namespace) {

        val existingNamespaceID = existingNodes.namespaceID
        val newNamespaceID = newNodes.namespaceID

        /* if the last node has been renamed or moved across namespaces, update it */
        when (NodeHelper.isRename(existingNodes, newNodes) || (existingNamespaceID != newNamespaceID)) {
            true -> { /* need to rename last node from existing path to last node from new path */
                renameNodeInNamespaceWithAccessValue(lastNodeID, newNodes.allNodes.last(), userID, workspaceID, newNamespaceID, targetNamespace.publicAccess.toInt())
            }
        }
    }

    private fun updateHierarchyInRefactorAndReturnDifference(
        sourceNamespace: Namespace,
        targetNamespace: Namespace,
        lastNodeHierarchy: List<String>,
        newPathTillLastNode: String,
        existingNodes: NodeNamePath,
        workspaceID: String
    ): MutableMap<String, Any> = runBlocking {

        val mapOfDifferenceOfPaths = mutableMapOf<String, Any>()
        val hierarchyOfSourceNamespace = sourceNamespace.nodeHierarchyInformation
        val hierarchyOfTargetNamespace = targetNamespace.nodeHierarchyInformation.toMutableList()

        val listOfChangedPaths = mutableListOf<Map<String, Any>>()

        /* when existing and target namespace are same, we have the following case :
           - Refactor within a single namespace

           Otherwise :
           - Refactor from One Namespace to Another Namespace
         */
        when (sourceNamespace == targetNamespace) {
            true -> { /* need to update only one hierarchy */
                val newHierarchyOfSourceNamespace = createNewHierarchyInRefactor(lastNodeHierarchy, hierarchyOfSourceNamespace, newPathTillLastNode, existingNodes)
                launch { updateNamespaceHierarchy(targetNamespace, newHierarchyOfSourceNamespace, HierarchyUpdateSource.NODE) }
                listOfChangedPaths.add(getMapOfDifferenceOfPaths(newHierarchyOfSourceNamespace, hierarchyOfSourceNamespace, sourceNamespace.id))
            }
            false -> { /* two hierarchies gets affected in this case */

                val updatedSourceNamespaceHierarchy = getUpdatedExistingHierarchy(hierarchyOfSourceNamespace, existingNodes)
                launch { updateNamespaceHierarchy(sourceNamespace, updatedSourceNamespaceHierarchy, HierarchyUpdateSource.NODE) }
                listOfChangedPaths.add(getMapOfDifferenceOfPaths(updatedSourceNamespaceHierarchy, hierarchyOfSourceNamespace, sourceNamespace.id))

                val updatedTargetNamespaceHierarchy = getNewHierarchyByAddingRefactoredPath(hierarchyOfTargetNamespace, lastNodeHierarchy, newPathTillLastNode)
                launch { updateNamespaceHierarchy(targetNamespace, updatedTargetNamespaceHierarchy, HierarchyUpdateSource.NODE) }
                listOfChangedPaths.add(getMapOfDifferenceOfPaths(updatedTargetNamespaceHierarchy, hierarchyOfTargetNamespace, targetNamespace.id))
            }
        }

        mapOfDifferenceOfPaths[Constants.CHANGED_PATHS] = listOfChangedPaths

        return@runBlocking mapOfDifferenceOfPaths
    }

    private fun updateNamespaceAndPublicAccessForSuccessorNodes(namespaceID: String, workspaceID: String, lastNodeHierarchy: List<String>, publicAccess: Int){
        val listOfNodeIDs = getNodeIDsFromHierarchy(lastNodeHierarchy)
        updateNamespaceAndPublicAccessOfNodesInParallel(listOfNodeIDs, workspaceID, namespaceID, publicAccess)
    }

    private fun createNewHierarchyInRefactor(lastNodeHierarchy: List<String>, currentHierarchy: List<String>, newPathTillLastNode: String, existingNodes: NodeNamePath): List<String> {

        /* remove partial paths from current hierarchy */
        val newHierarchy = getNewHierarchyByRemovingPassedNamePath(currentHierarchy, existingNodes)

        /* add ( longestExistingPath + path due to new nodes(if any) + last node + last node hierarchy ) to newHierarchy/targetHierarchy ) */
        return getNewHierarchyByAddingRefactoredPath(newHierarchy, lastNodeHierarchy, newPathTillLastNode)
    }

    private fun getUpdatedExistingHierarchy(currentHierarchy: List<String>, existingNodes: NodeNamePath): MutableList<String> {
        return getNewHierarchyByRemovingPassedNamePath(currentHierarchy, existingNodes)
    }

    private fun getNewHierarchyByAddingRefactoredPath(currentHierarchy: List<String>, lastNodeHierarchy: List<String>, newPathTillLastNode: String): List<String> {
        val updatedHierarchy = currentHierarchy.toMutableList()

        /* if the last node passed in refactor request does not have further notes, add new path till last node */
        if (lastNodeHierarchy.isEmpty()) updatedHierarchy.add(newPathTillLastNode)
        else {
            for (lastNodeHierarchyPath in lastNodeHierarchy) {
                updatedHierarchy.add(newPathTillLastNode.createNodePath(lastNodeHierarchyPath))
            }
        }

        return removeRedundantPaths(updatedHierarchy)
    }

    private fun getNewHierarchyByRemovingPassedNamePath(currentHierarchy: List<String>, existingNodes: NodeNamePath): MutableList<String> {
        val newHierarchy = mutableListOf<String>()
        /* collect paths which need to be updated */
        val updatedPaths = mutableListOf<String>()
        for (path in currentHierarchy) {
            /* check if the name path in current hierarchy matches passed existing nodes */
            if (getNamePath(path).getListOfNodes().commonPrefixList(existingNodes.allNodes) == existingNodes.allNodes) {
                /* break the connection from last node */
                updatedPaths.addIfNotEmpty(path.getListOfNodes().subList(0, path.getListOfNodes().indexOf(existingNodes.allNodes.last())).convertToPathString())
            } else {
                newHierarchy.addIfNotEmpty(path)
            }
        }
        removeRedundantPaths(updatedPaths, newHierarchy)
        return newHierarchy
    }

    /* used for refactor */
    private fun addPathsAndCreateNodesBeforeLastNode(
        refactorNodePathRequest: RefactorRequest,
        paths: MutableList<String>,
        userID: String,
        targetNamespace: Namespace,
        workspaceID: String
    ) {
        val newNodesWithoutLast = NodeNamePath(
            path = refactorNodePathRequest.newNodePath.allNodes.dropLast(1).convertToPathString(),
            namespaceID = refactorNodePathRequest.newNodePath.namespaceID
        )

        val longestExistingPath = NodeHelper.getLongestExistingPathFromNamePath(newNodesWithoutLast.path, targetNamespace.nodeHierarchyInformation)

        paths.addIfNotEmpty(longestExistingPath)

        var listOfNodesToCreate = mutableListOf<Node>()
        try {
            listOfNodesToCreate = setMetaDataForEmptyNodes(getNodesToCreate(longestExistingPath, newNodesWithoutLast.path).toMutableList(),
                    userID, workspaceID, targetNamespace)

        } catch (e: IllegalArgumentException) {
            // don't do anything. Just a  case where only renaming has been done, or we're appending to an already existing path
        }

        nodeRepository.createMultipleNodes(listOfNodesToCreate)

        /* path from new nodes to be created (will either be a suffix or an independent string)*/
        val pathForNewNodes = getSuffixPathInBulkCreate(listOfNodesToCreate)

        paths.addIfNotEmpty(pathForNewNodes)
    }

    private fun getHierarchyOfNode(currentHierarchy: List<String>, nodeID: String): List<String> {

        val listOfPaths = mutableListOf<String>()
        for (nodePath in currentHierarchy) {
            if (nodePath.contains(nodeID)) {
                listOfPaths.addIfNotEmpty(getPathAfterNode(nodePath, nodeID))
            }
        }
        return listOfPaths
    }

    private fun getPathAfterNode(nodePath: String, nodeID: String): String {
        return nodePath.getListOfNodes().subList(nodePath.getListOfNodes(Constants.DELIMITER).indexOf(nodeID) + 1, nodePath.getListOfNodes().size).convertToPathString()
    }

    private fun renameNodeInNamespaceWithAccessValue(nodeID: String, newName: String, userID: String, workspaceID: String, namespaceID: String, publicAccess: Int) {
        nodeRepository.renameNodeInNamespaceWithAccessValue(nodeID, newName, userID, workspaceID, namespaceID, publicAccess)
    }

    private fun setMetaDataForEmptyNodes(
        namesOfNodesToCreate: MutableList<String>,
        lastEditedBy: String,
        workspaceID: String,
        namespace: Namespace,
    ): MutableList<Node> {
        val listOfNodes = mutableListOf<Node>()
        for (newNodeName in namesOfNodesToCreate) {
            listOfNodes.add(
                Node(
                    id = Helper.generateNanoID(IdentifierType.NODE.name),
                    title = newNodeName,
                    workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                    namespaceIdentifier = NamespaceIdentifier(namespace.id),
                    publicAccess = namespace.publicAccess,
                    createdBy = lastEditedBy,
                    lastEditedBy = lastEditedBy,
                    data = listOf()
                )
            )
        }

        return listOfNodes
    }

    fun bulkCreateNodes(request: WDRequest, workspaceID: String, userID: String): Map<String, Any> = runBlocking {
        val nodeRequest: NodeBulkRequest = request as NodeBulkRequest

        val nodePath: NodePath = nodeRequest.nodePath

        /* this call internally checks user access and throw error */
        val nodeWorkspaceID = namespaceService
            .namespaceAccessService
            .checkIfUserHasAccessAndGetWorkspaceDetails(nodeRequest.nodePath.namespaceID, workspaceID, userID, EntityOperationType.WRITE)[Constants.WORKSPACE_ID]!!

        val node: Node = createNodeObjectFromNodeBulkRequest(nodeRequest, nodePath.allNodesNames.last(), nodePath.allNodesIDs.last(), nodeWorkspaceID, userID)


        val jobToGetNamespace = async {
            node.namespaceIdentifier.id.let { namespaceID ->
                namespaceService.getNamespaceAfterPermissionCheck(namespaceID).let { namespace ->
                    require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
                    namespace
                }
            }
        }

        val namespace: Namespace = jobToGetNamespace.await()
        val namespaceHierarchy = namespace.nodeHierarchyInformation

        NodeHelper.checkForDuplicateNodeID(namespaceHierarchy, node.id)

        val longestExistingPath = updateNodePath(NodeHelper.getLongestExistingPathFromNamePath(getNamePath(nodePath.path), namespaceHierarchy), nodePath, node)

        val listOfNodes = getListOfNodesToCreateInBulkCreate(nodePath, longestExistingPath, node, namespace)

        /* path from new nodes to be created (will either be a suffix or an independent string)*/
        val suffixNodePath = getSuffixPathInBulkCreate(listOfNodes)

        val updatedNodeHierarchy =
            getUpdatedNodeHierarchyInBulkCreate(namespaceHierarchy.toMutableList(), longestExistingPath, suffixNodePath)

        launch { nodeRepository.createMultipleNodes(listOfNodes) }

        launch { updateNamespaceHierarchy(namespace, updatedNodeHierarchy, HierarchyUpdateSource.NODE) }

        val mapOfNodeAndDifference = mutableMapOf(Constants.NODE to (node as Any)) /* requirement of middleware */

        /* converting map on RHS to List so that response looks in line with refactor */
        mapOfNodeAndDifference[Constants.CHANGED_PATHS] = listOf(getMapOfDifferenceOfPaths(updatedNodeHierarchy, namespaceHierarchy, namespace.id))
        return@runBlocking mapOfNodeAndDifference
    }

    private fun getMapOfDifferenceOfPaths(updatedNodeHierarchy: List<String>, oldHierarchy: List<String>, namespaceID: String): MutableMap<String, Any> {
        val mapOfChangedPaths = mutableMapOf<String, Any>()
        mapOfChangedPaths[namespaceID] = updatedNodeHierarchy.getDifferenceWithOldHierarchy(oldHierarchy)
        return mapOfChangedPaths
    }

    private fun updateNamespaceHierarchy(namespace: Namespace, updatedNodeHierarchy: List<String>, updateSource: HierarchyUpdateSource) {
        namespaceService.updateNamespaceHierarchy(
            namespace,
            updatedNodeHierarchy,
            updateSource
        )
    }

    private fun getSuffixPathInBulkCreate(listOfNodes: List<Node>): String {
        var suffixNodePath = ""
        for ((index, nodeToCreate) in listOfNodes.withIndex()) {
            when (index) {
                0 -> suffixNodePath = "${nodeToCreate.title}${Constants.DELIMITER}${nodeToCreate.id}"
                else -> suffixNodePath += "${Constants.DELIMITER}${nodeToCreate.title}${Constants.DELIMITER}${nodeToCreate.id}"
            }
        }
        return suffixNodePath
    }


    private fun getListOfNodesToCreateInBulkCreate(nodePath: NodePath, longestExistingPath: String, node: Node, namespace: Namespace): List<Node> {

        val nodesToCreate: List<String> = getNodesToCreate(longestExistingPath, getNamePath(nodePath.path))
        setMetadataOfNodeToCreate(node, namespace) /* last node */
        return setMetaDataFromNode(node, nodesToCreate, nodePath.allNodesIDs.takeLast(nodesToCreate.size))
    }

    private fun getNodesToCreate(longestExistingPath: String, nodePath: String): List<String> {
        val longestExistingNamePath = getNamePath(longestExistingPath)

        return nodePath.getListOfNodes().removePrefixList(longestExistingNamePath.getListOfNodes())
    }

    private fun getUpdatedNodeHierarchyInBulkCreate(
        existingHierarchy: MutableList<String>,
        longestExistingPath: String,
        suffixNodePath: String /* path to be added to longestExistingPath */
    ): List<String> {

        var nodePathToRemove: String? = null

        for (existingNodePath in existingHierarchy) {
            if (longestExistingPath == existingNodePath) {
                nodePathToRemove = existingNodePath
                break
            }
        }

        /* if nodePathToRemove != null , we are adding to a leaf path, so we remove that path*/
        when (nodePathToRemove != null) {
            true -> existingHierarchy.remove(nodePathToRemove)
        }

        existingHierarchy.add(longestExistingPath.createNodePath(suffixNodePath))

        return existingHierarchy
    }

    fun setMetaDataFromNode(node: Node, nodesToCreate: List<String>?, nodeIDList: List<String>): List<Node> {

        val listOfNodes = mutableListOf<Node>()
        nodesToCreate?.let {
            for (index in 0 until nodesToCreate.size - 1) { /* since the last element is the node itself */
                listOfNodes.add(createEmptyNodeWithMetadata(node, nodesToCreate[index], nodeIDList[index]))
            }
        }

        listOfNodes.add(node)

        return listOfNodes
    }

    private fun createEmptyNodeWithMetadata(node: Node, newNodeName: String, nodeID: String): Node {
        val newNode = Node(
            id = nodeID,
            title = newNodeName,
            workspaceIdentifier = node.workspaceIdentifier,
            namespaceIdentifier = node.namespaceIdentifier,
            createdBy = node.createdBy,
            lastEditedBy = node.lastEditedBy,
            createdAt = node.createdAt,
            data = listOf()
        )

        newNode.updatedAt = node.updatedAt

        return newNode
    }

    /* sets AK, dataOrder, createdBy and accountability data of blocks of the node */
    private fun setMetadataOfNodeToCreate(node: Node, namespace: Namespace) {
        node.dataOrder = createDataOrderForNode(node)
        node.publicAccess = namespace.publicAccess
        /* only when node is actually being created */
        node.createdBy = node.lastEditedBy

        for (e in node.data!!) {
            e.createdBy = node.lastEditedBy
            e.lastEditedBy = node.lastEditedBy
            e.createdAt = node.createdAt
            e.updatedAt = node.createdAt
        }
    }

    private fun createDataOrderForNode(node: Node): MutableList<String> {

        val list = mutableListOf<String>()
        for (element in node.data!!) {
            list += element.id
        }
        return list
    }

    fun getNode(nodeID: String, userWorkspaceID: String, userID: String, itemStatus: ItemStatus? = null, starredInfo: Boolean = false) = runBlocking {

        require(nodeAccessService.getNamespaceIDAndCheckIfUserHasAccess(userWorkspaceID, nodeID, userID, EntityOperationType.READ)) {
            Messages.ERROR_NODE_PERMISSION
        }

        getNodeAfterPermissionCheck(nodeID, userID, itemStatus, starredInfo)

    }

    fun getNodeAfterPermissionCheck(nodeID: String, userID: String, itemStatus: ItemStatus? = null, starredInfo: Boolean = false) = runBlocking {
        /* to avoid fetching node's workspace first, directly use GSI to get node by node ID */
        val node = nodeRepository.getNodeByNodeID(nodeID, itemStatus)

        val jobToGetStarredStatus = async {
            when (starredInfo && node != null) {
                true -> UserStarService().isNodeStarredForUser(nodeID, userID, node.workspaceIdentifier.id)
                false -> null
            }
        }

        return@runBlocking node?.let { it ->
            (orderBlocks(it) as Node).also { orderedNode ->
                orderedNode.starred = jobToGetStarredStatus.await()
            }
        }
    }

    fun getNodesInBatch(nodeIDRequest: WDRequest, workspaceID: String): List<Node> {
        val nodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()
        require(nodeIDList.size < Constants.MAX_NODE_IDS_FOR_BATCH_GET) { "Number of NodeIDs should be lesser than ${Constants.MAX_NODE_IDS_FOR_BATCH_GET}" }
        return nodeRepository.batchGetNodes(nodeIDList, workspaceID)
    }

    fun archiveNodesSupportedByStreams(nodeIDRequest: WDRequest, workspaceID: String): MutableList<String> = runBlocking {

        val nodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        val jobToGetWorkspace = async { workspaceService.getWorkspace(workspaceID) as Workspace }
        val jobToChangeNodeStatus =
            async { pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ARCHIVED) }

        val workspace = jobToGetWorkspace.await()

        // TODO(start using coroutines here if requests contain a lot of node ids)
        for (nodeID in nodeIDList) {
            workspaceService.updateNodeHierarchyOnArchivingNode(workspace, nodeID)
        }
        return@runBlocking jobToChangeNodeStatus.await()
    }

    fun archiveNodes(nodeIDRequest: WDRequest, workspaceID: String, namespaceID: String, userID: String): List<String> {
        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        // permission for the user would be checked when fetching the namespace
        val namespace = namespaceService.getNamespace(workspaceID, namespaceID, userID).let { namespace ->
            require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
            namespace
        }

        val currentActiveHierarchy = namespace.nodeHierarchyInformation

        updateHierarchiesInArchive(namespace, passedNodeIDList)

        val nodeIDsToArchive = getRemovedNodeIDs(currentActiveHierarchy, namespace.nodeHierarchyInformation)
        unarchiveOrArchiveNodesInParallel(nodeIDsToArchive, workspaceID, ItemStatus.ARCHIVED)

        return nodeIDsToArchive
    }

    fun archiveNodesMiddleware(nodeIDRequest: WDRequest, workspaceID: String, namespaceID: String, userID: String): MutableMap<String, List<String>> {

        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        // permission for the user would be checked when fetching the namespace
        val namespace = namespaceService.getNamespace(workspaceID, namespaceID, userID).let { namespace ->
            require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
            namespace
        }

        val currentActiveHierarchy = namespace.nodeHierarchyInformation

        updateHierarchiesInArchive(namespace, passedNodeIDList)

        val nodeIDsToArchive = getRemovedNodeIDs(currentActiveHierarchy, namespace.nodeHierarchyInformation)

        unarchiveOrArchiveNodesInParallel(nodeIDsToArchive, workspaceID, ItemStatus.ARCHIVED)

        /* mapOfArchivedHierarchyAndActiveHierarchyDiff */
        return mutableMapOf(Constants.ARCHIVED_HIERARCHY to namespace.archivedNodeHierarchyInformation).also {
            it.putAll(namespace.nodeHierarchyInformation.getDifferenceWithOldHierarchy(currentActiveHierarchy))
        }
    }

    // TODO( implement the behavior for renaming of nodes while un-archiving in case of clashing names at topmost level )
    fun unarchiveNodesNew(nodeIDRequest: WDRequest, workspaceID: String, namespaceID: String, userID: String): MutableMap<String, List<String>> {
        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        // permission for the user would be checked when fetching the namespace
        val namespace = namespaceService.getNamespace(workspaceID, namespaceID, userID).let { namespace ->
            require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
            namespace
        }

        val currentActiveHierarchy = namespace.nodeHierarchyInformation
        val currentArchivedHierarchy = namespace.archivedNodeHierarchyInformation

        updateHierarchiesInUnarchive(namespace, passedNodeIDList)

        /* compare old and new archived hierarchies */
        val nodeIDsToUnarchive = getRemovedNodeIDs(currentArchivedHierarchy, namespace.archivedNodeHierarchyInformation)

        unarchiveOrArchiveNodesInParallel(nodeIDsToUnarchive, workspaceID, ItemStatus.ACTIVE)

        /* mapOfArchivedHierarchyAndActiveHierarchyDiff */
        return mutableMapOf(Constants.ARCHIVED_HIERARCHY to namespace.archivedNodeHierarchyInformation).also {
            it.putAll(namespace.nodeHierarchyInformation.getDifferenceWithOldHierarchy(currentActiveHierarchy))
        }
    }

    fun getRemovedNodeIDs(oldHierarchy: List<String>, newHierarchy: List<String>): List<String> {
        val oldNodeIDs = getNodeIDsFromHierarchy(oldHierarchy)
        val newNodeIDs = getNodeIDsFromHierarchy(newHierarchy)
        return oldNodeIDs.filter { oldNodeID ->
            newNodeIDs.all { it != oldNodeID }
        }
    }

    fun unarchiveNodesOld(nodeIDRequest: WDRequest, workspaceID: String, namespaceID: String, userID: String): List<String> = runBlocking {
        val nodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList().toMutableList()
        val mapOfNodeIDToName = getArchivedNodesToRename(nodeIDList, workspaceID, namespaceID, userID)

        LOG.debug(mapOfNodeIDToName)
        for ((nodeID, _) in mapOfNodeIDToName) {
            nodeIDList.remove(nodeID)
        }

        val jobToUnarchiveAndRenameNodes = async { nodeRepository.unarchiveAndRenameNodes(mapOfNodeIDToName, workspaceID) }

        val jobToUnarchiveNodes =
            async { pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ACTIVE) }

        return@runBlocking jobToUnarchiveAndRenameNodes.await() + jobToUnarchiveNodes.await()
    }

    private fun unarchiveOrArchiveNodesInParallel(nodeIDList: List<String>, workspaceID: String, itemStatus: ItemStatus) = runBlocking {

        val jobToArchive = CoroutineScope(Dispatchers.IO + Job()).async {
            supervisorScope {
                val deferredList = ArrayList<Deferred<*>>()
                for (nodeID in nodeIDList) {
                    deferredList.add(
                        async { pageRepository.unarchiveOrArchivePages(listOf(nodeID), workspaceID, itemStatus) }
                    )
                }
                deferredList.joinAll()
            }
        }

        jobToArchive.await()
    }

    fun makeNodesPublicOrPrivateInParallel(nodeIDList: List<String>, workspaceID: String, accessValueToSet: Int) = runBlocking {
        val jobToArchive = CoroutineScope(Dispatchers.IO + Job()).async {
            supervisorScope {
                val deferredList = ArrayList<Deferred<*>>()
                for (nodeID in nodeIDList) {
                    deferredList.add(
                        async { pageRepository.togglePagePublicAccess(nodeID, workspaceID, accessValueToSet) }
                    )
                }
                deferredList.joinAll()
            }
        }
        jobToArchive.await()
    }

    private fun updateNamespaceAndPublicAccessOfNodesInParallel(nodeIDList: List<String>, workspaceID: String, namespaceID: String, publicAccess: Int)  = runBlocking{

        val jobToUpdateNamespace = CoroutineScope(Dispatchers.IO + Job()).async {
            supervisorScope {
                val deferredList = ArrayList<Deferred<*>>()
                for (nodeID in nodeIDList) {
                    deferredList.add(
                            async {  nodeRepository.updateNodeNamespaceAndPublicAccess(nodeID, workspaceID, namespaceID, publicAccess) }
                    )
                }
                deferredList.joinAll()
            }
        }

        jobToUpdateNamespace.await()
    }

    private fun updateHierarchiesInArchive(namespace: Namespace, passedNodeIDList: List<String>) {

        val activeHierarchy = namespace.nodeHierarchyInformation
        require(activeHierarchy.isNotEmpty()) { "Hierarchy does not exist" }

        val newArchivedHierarchy = namespace.archivedNodeHierarchyInformation.toMutableList()
        val newActiveHierarchy = mutableListOf<String>()

        updateHierarchiesInArchiveUnarchive(activeHierarchy, newActiveHierarchy, newArchivedHierarchy, passedNodeIDList)

        Namespace.populateHierarchiesAndUpdatedAt(namespace, newActiveHierarchy, newArchivedHierarchy)
        namespaceService.updateNamespace(namespace)
    }

    private fun updateHierarchiesInUnarchive(namespace: Namespace, passedNodeIDList: List<String>) {

        val archivedHierarchy = namespace.archivedNodeHierarchyInformation
        require(archivedHierarchy.isNotEmpty()) { "Archived hierarchy does not exist" }

        val newActiveHierarchy = namespace.nodeHierarchyInformation.toMutableList()
        val newArchivedHierarchy = mutableListOf<String>()

        updateHierarchiesInArchiveUnarchive(archivedHierarchy, newArchivedHierarchy, newActiveHierarchy, passedNodeIDList)

        Namespace.populateHierarchiesAndUpdatedAt(namespace, newActiveHierarchy, newArchivedHierarchy)
        namespaceService.updateNamespace(namespace)
    }

    /* sourceHierarchy : Hierarchy to move nodes from.
       In case of archiving, sourceHierarchy will be active hierarchy
       In case of unarchiving, sourceHierarchy will be archived hierarchy

       newSourceHierarchy : Updated Hierarchy from which nodes were moved.
       In case of archiving, newSourceHierarchy will be newActiveHierarchy hierarchy
       In case of unarchiving, newSourceHierarchy will be newArchivedHierarchy hierarchy


       newDestinationHierarchy : Updated Hierarchy to which nodes were moved.
       In case of archiving, newDestinationHierarchy will be newArchivedHierarchy hierarchy
       In case of unarchiving, newDestinationHierarchy will be newActiveHierarchy hierarchy

     */
    private fun updateHierarchiesInArchiveUnarchive(
        sourceHierarchy: List<String>,
        newSourceHierarchy: MutableList<String>,
        newDestinationHierarchy: MutableList<String>,
        passedNodeIDList: List<String>
    ) {

        for (nodePath in sourceHierarchy) {
            var isNodePresentInPath = false
            val pathsListForSinglePath = mutableListOf<String>() /* more than one node ids from a single path could be passed */
            for (nodeID in passedNodeIDList) {
                if (nodePath.contains(nodeID)) {
                    isNodePresentInPath = true
                    pathsListForSinglePath.add(
                        nodePath.getListOfNodes().let {
                            it.subList(it.indexOf(nodeID) - 1, it.size)
                        }.convertToPathString()
                    )
                }
            }
            if (isNodePresentInPath) {
                val finalPathToArchive = removeRedundantPaths(pathsListForSinglePath, MatchType.SUFFIX)[0]
                newDestinationHierarchy.add(finalPathToArchive)
                /* active hierarchy is nodePath minus the archived path */
                newSourceHierarchy.addIfNotEmpty(nodePath.getListOfNodes().dropLast(finalPathToArchive.getListOfNodes().size).convertToPathString())
            } else { /* this path will remain unchanged */
                newSourceHierarchy.add(nodePath)
            }
        }

        removeRedundantPaths(newDestinationHierarchy)
        removeRedundantPaths(newSourceHierarchy)
    }

    /* Getting called Internally via trigger. No need to update hierarchy */
    fun archiveNodes(nodeIDList: List<String>, workspaceID: String) = runBlocking {
        pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ARCHIVED)
    }

    fun getAllNodeIDToNodeNameMap(workspaceID: String, itemStatus: ItemStatus): Map<String, String> {
        return nodeRepository.getAllNodeIDToNodeNameMap(workspaceID, itemStatus)
    }

    fun append(nodeID: String, workspaceID: String, userID: String, elementsListRequest: WDRequest): Map<String, Any>? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val elements = elementsListRequestConverted.elements

        val orderList = mutableListOf<String>()
        for (e in elements) {
            orderList += e.id

            e.lastEditedBy = userID
            e.createdAt = Constants.getCurrentTime()
            e.updatedAt = e.createdAt
        }
        return nodeRepository.append(nodeID, workspaceID, userID, elements, orderList)
    }

    fun updateNode(node: Node, storedNode: Node) : Node = runBlocking {

        Page.populatePageWithCreatedAndPublicFields(node, storedNode)

        node.dataOrder = createDataOrderForPage(node)

        if (node.isNodeUnchanged(storedNode)) {
            return@runBlocking storedNode
        }

        /* to make the locking versions same */
        mergePageVersions(node, storedNode)

        launch { updateHierarchyIfRename(node, storedNode) }

        launch { updateTags(node.tags, storedNode.tags, node.id, node.workspaceIdentifier.id) }

        launch { repository.update(node) }
        return@runBlocking node

    }

    private fun updateHierarchyIfRename(node: Node, storedNode: Node) {
        val newHierarchy = mutableListOf<String>()
        if (node.title != storedNode.title) {
            val workspace = workspaceService.getWorkspace(node.workspaceIdentifier.id) as Workspace
            val currentHierarchy = workspace.nodeHierarchyInformation ?: listOf()
            for (nodePath in currentHierarchy) {
                val idList = getIDPath(nodePath).getListOfNodes()
                val indexOfNodeID = idList.indexOf(node.id)
                if (indexOfNodeID != -1) {
                    val nameList = getNamePath(nodePath).getListOfNodes().toMutableList()
                    nameList[indexOfNodeID] = node.title
                    newHierarchy.add(nameList.mix(idList).convertToPathString())
                } else {
                    newHierarchy.add(nodePath)
                }
            }
            workspaceService.updateWorkspaceHierarchy(workspace, newHierarchy, HierarchyUpdateSource.RENAME)
        }
    }

    fun checkNodeVersionCount(node: Node, storedNodeVersionCount: Long) {
        if (storedNodeVersionCount < 25) {
            node.nodeVersionCount = storedNodeVersionCount + 1
            println(Thread.currentThread().id)
        } else {
            node.nodeVersionCount = storedNodeVersionCount + 1
            setTTLForOldestVersion(node.id)
        }
    }

    private fun setTTLForOldestVersion(nodeID: String) {

        /*returns first element from sorted updatedAts in ascending order */
        val oldestUpdatedAt = getMetaDataForActiveVersions(nodeID)?.get(0)

        println(oldestUpdatedAt)

        if (oldestUpdatedAt != null)
            nodeRepository.setTTLForOldestVersion(nodeID, oldestUpdatedAt)
    }

    fun getMetaDataForActiveVersions(nodeID: String): MutableList<String>? {
        return nodeRepository.getMetaDataForActiveVersions(nodeID)
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String> {

        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithUserID(userID: String): List<String> {
        return nodeRepository.getAllNodesWithUserID(userID)
    }

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): List<String> {
        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)
    }

    fun getAllNodesWithNamespaceIDAndAccess(namespaceID: String, workspaceID: String, publicAccess: Int): List<String> {
        return nodeRepository.getAllNodesWithNamespaceIDAndAccess(namespaceID, workspaceID, publicAccess)
    }

    fun batchDeleteNodes(listOfNodeIDs : List<String>, workspaceID: String){
        val nodesWithValidPKSK = listOfNodeIDs.map { nodeID ->
            Node( id = nodeID,
                  workspaceIdentifier = WorkspaceIdentifier(workspaceID))
        }

        nodeRepository.deleteBatchNodes(nodesWithValidPKSK)

    }

    fun updateNodeBlock(nodeID: String, workspaceID: String, userID: String, elementsListRequest: WDRequest): AdvancedElement? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val element = elementsListRequestConverted.elements.let { it[0] }

        element.updatedAt = Constants.getCurrentTime()

        // TODO(since we directly set the block info, createdAt and createdBy get lost since we're not getting anything from ddb)
        val blockData = objectMapper.writeValueAsString(element)

        return nodeRepository.updateNodeBlock(nodeID, workspaceID, blockData, element.id, userID)
    }

    private fun createNodeObjectFromNodeRequest(nodeRequest: NodeRequest, workspaceID: String, userID: String): Node =
        nodeRequest.toNode(workspaceID, userID).also {
            if (it.getRoughSizeOfEntity() > Constants.DDB_MAX_ITEM_SIZE) throw WDNodeSizeLargeException("Node size is too large")
        }

    private fun createNodeObjectFromUpdateShareNodeRequest(nodeRequest: UpdateSharedNodeRequest, workspaceID: String, namespaceID: String, userID: String): Node =
            nodeRequest.toNode(workspaceID, namespaceID, userID).also {
                if(it.getRoughSizeOfEntity() > Constants.DDB_MAX_ITEM_SIZE)  throw WDNodeSizeLargeException("Node size is too large")
            }

    private fun createNodeObjectFromNodeBulkRequest(nodeBulkRequest: NodeBulkRequest, nodeTitle: String,
                                                    nodeID: String, workspaceID: String, userID: String): Node =
        nodeBulkRequest.toNode(nodeID, nodeTitle, workspaceID, userID)

    fun getAllArchivedNodeIDsOfWorkspace(workspaceID: String): MutableList<String> {
        return pageRepository.getAllArchivedPagesOfWorkspace(workspaceID, ItemType.Node)
    }

    /* this is called internally via trigger. We don't need to do sanity check for name here */
    fun unarchiveNodes(nodeIDList: List<String>, workspaceID: String): MutableList<String> {
        return pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ACTIVE)
    }

    /* to ensure that nodes at same level don't have same name */
    private fun getArchivedNodesToRename(nodeIDList: List<String>, workspaceID: String, namespaceID: String, userID: String): Map<String, String> =
        runBlocking {

            val jobToGetWorkspace = async { namespaceService.getNamespace(workspaceID, namespaceID, userID) }

            val jobToGetArchivedNodeIDToNameMap = async { getAllNodeIDToNodeNameMap(workspaceID, ItemStatus.ARCHIVED) }
            val jobToGetArchivedHierarchyRelationship =
                async { RelationshipService().getHierarchyRelationshipsOfWorkspace(workspaceID, ItemStatus.ARCHIVED) }

            val nodeHierarchyInformation = jobToGetWorkspace.await()?.nodeHierarchyInformation ?: listOf()
            val archivedNodeIDToNameMap = jobToGetArchivedNodeIDToNameMap.await()
            val archivedHierarchyRelationships = jobToGetArchivedHierarchyRelationship.await()

            val mapOfNodeIDToNodeName = mutableMapOf<String, String>()

            /* nodeIDList contains all the nodeIds to be un-archived */
            for (nodeID in nodeIDList) {
                val archivedNodeName = archivedNodeIDToNameMap[nodeID]
                    ?: throw Exception("Invalid nodeID : $nodeID")
                val archivedNodeParentID = findStartNodeOfEndNode(archivedHierarchyRelationships, nodeID)

                for (nodePath in nodeHierarchyInformation) {
                    if (nodePath.contains(archivedNodeName)) { /* if there's an existing node with same name as node to be un-archived */

                        when (archivedNodeParentID == null) {
                            true -> { /* node to be un-archived is a root node */
                                if (nodePath.startsWith(archivedNodeName)) { /* if there's an existing node with same name at root level */
                                    mapOfNodeIDToNodeName[nodeID] = archivedNodeName
                                }
                            }
                            false -> {
                                val activeNodesInPath = nodePath.split(Constants.DELIMITER)
                                val indexOfParentNode = activeNodesInPath.indexOf(archivedNodeParentID)
                                /* if for some parent node, there exists a node with name "A" and node to be unarchived has same parent node with same name "A"*/
                                if (indexOfParentNode != -1 && indexOfParentNode + 1 < activeNodesInPath.size &&
                                    activeNodesInPath[indexOfParentNode + 1] == archivedNodeName
                                ) {
                                    mapOfNodeIDToNodeName[nodeID] = archivedNodeName
                                }
                            }
                        }
                    }
                }
            }
            return@runBlocking mapOfNodeIDToNodeName
        }

    fun deleteArchivedNodes(nodeIDRequest: WDRequest, workspaceID: String): MutableList<String> = runBlocking {

        val nodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()
        require(getAllArchivedNodeIDsOfWorkspace(workspaceID).containsAll(nodeIDList)) { "The passed IDs should be present and archived" }
        val deletedNodesList: MutableList<String> = mutableListOf()
        for (nodeID in nodeIDList) {
            val tags = nodeRepository.getTags(nodeID, workspaceID)
            if (!tags.isNullOrEmpty()) launch { deleteTags(tags, nodeID, workspaceID) }
            repository.delete(WorkspaceIdentifier(workspaceID), NodeIdentifier(nodeID)).also {
                deletedNodesList.add(it.id)
            }
        }
        return@runBlocking deletedNodesList
    }

    fun makeNodePublic(nodeID: String, userWorkspaceID: String, userID: String) {
        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.MANAGE)
        pageRepository.togglePagePublicAccess(nodeID, nodeWorkspaceID, 1)
    }

    fun makeNodePrivate(nodeID: String, userWorkspaceID: String, userID: String) {
        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.MANAGE)
        pageRepository.togglePagePublicAccess(nodeID, nodeWorkspaceID, 0)
    }

    fun getPublicNode(nodeID: String): Node {
        val publicNodeWriteCache = NodeCache(System.getenv("PUBLIC_NOTE_CACHE_ENDPOINT") ?: Constants.DEFAULT_PUBLIC_NOTE_CACHE_ENDPOINT)
        val publicNodeReadCache = NodeCache(System.getenv("PUBLIC_NOTE_CACHE_READER_ENDPOINT") ?: Constants.DEFAULT_PUBLIC_NOTE_CACHE_ENDPOINT)
        try {
            val node = publicNodeReadCache.getNode(nodeID) ?: let {
                val nodeFromDB = orderBlocks(pageRepository.getPublicPage(nodeID, Node::class.java)) as Node
                publicNodeWriteCache.setNode(nodeID, nodeFromDB)
                return nodeFromDB
            }
            return orderBlocks(node) as Node
        } finally {
            publicNodeReadCache.closeConnection()
            publicNodeWriteCache.closeConnection()
        }
    }

    fun copyOrMoveBlock(wdRequest: WDRequest, workspaceID: String) {

        val copyOrMoveBlockRequest = wdRequest as BlockMovementRequest
        val destinationNodeID = copyOrMoveBlockRequest.destinationNodeID
        val sourceNodeID = copyOrMoveBlockRequest.sourceNodeID
        val blockID = copyOrMoveBlockRequest.blockID

        workspaceLevelChecksForMovement(destinationNodeID, sourceNodeID, workspaceID)

        when (copyOrMoveBlockRequest.action.lowercase()) {
            "copy" -> copyBlock(blockID, workspaceID, sourceNodeID, destinationNodeID)
            "move" -> moveBlock(blockID, workspaceID, sourceNodeID, destinationNodeID)
            else -> throw IllegalArgumentException("Invalid action")
        }
    }

    private fun workspaceLevelChecksForMovement(destinationNodeID: String, sourceNodeID: String, workspaceID: String) = runBlocking {
        require(destinationNodeID != sourceNodeID) {
            Messages.SOURCE_ID_DESTINATION_ID_SAME
        }

        val jobToGetSourceNodeWorkspaceID = async { nodeAccessService.checkIfNodeExistsForWorkspace(sourceNodeID, workspaceID) }
        val jobToGetDestinationNodeWorkspaceID = async { nodeAccessService.checkIfNodeExistsForWorkspace(destinationNodeID, workspaceID) }

        jobToGetSourceNodeWorkspaceID.awaitAndThrowExceptionIfFalse(jobToGetDestinationNodeWorkspaceID, Messages.NODE_IDS_DO_NOT_EXIST)
    }

    private fun copyBlock(blockID: String, workspaceID: String, sourceNodeID: String, destinationNodeID: String) {
        /* this node contains only valid block info and dataOrder info */
        val sourceNode: Node? = nodeRepository.getBlock(sourceNodeID, blockID, workspaceID)

        val block = sourceNode?.data?.get(0)
        val userID = block?.createdBy as String

        nodeRepository.append(destinationNodeID, workspaceID, userID, listOf(block), mutableListOf(block.id))
    }

    private fun moveBlock(blockID: String, workspaceID: String, sourceNodeID: String, destinationNodeID: String) {
        /* this node contains only valid block info and dataOrder info  */
        val sourceNode: Node? = nodeRepository.getBlock(sourceNodeID, blockID, workspaceID)

        // TODO(list remove changes order of the original elements )
        sourceNode?.dataOrder?.let {
            it.remove(blockID)
            nodeRepository.moveBlock(sourceNode.data?.get(0), workspaceID, sourceNodeID, destinationNodeID, it)
        }
    }

    fun shareNode(wdRequest: WDRequest, granterID: String, granterWorkspaceID: String) {
        val sharedNodeRequest = wdRequest as SharedNodeRequest
        val userIDs = AccessItemHelper.getUserIDsWithoutGranterID(sharedNodeRequest.userIDs, granterID) // remove granterID from userIDs if applicable.

        if (userIDs.isEmpty()) return

        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(granterWorkspaceID, sharedNodeRequest.nodeID, granterID, EntityOperationType.MANAGE)
        val nodeAccessItems = getNodeAccessItems(sharedNodeRequest.nodeID, nodeWorkspaceID, granterID, userIDs, sharedNodeRequest.accessType)
        nodeRepository.createBatchNodeAccessItem(nodeAccessItems)
    }

    fun getSharedNode(nodeID: String, userID: String): Entity {
        require(nodeRepository.checkIfAccessRecordExists(nodeID, userID)) { Messages.ERROR_NODE_PERMISSION }
        return nodeRepository.getNodeByNodeID(nodeID, ItemStatus.ACTIVE)?.let {
            orderBlocks(it)
        } ?: throw NoSuchElementException(Messages.INVALID_NODE_ID)

    }

    fun changeAccessType(wdRequest: WDRequest, granterID: String, granterWorkspaceID: String) {
        val updateAccessRequest = wdRequest as UpdateAccessTypesRequest
        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(granterWorkspaceID, updateAccessRequest.nodeID, granterID, EntityOperationType.MANAGE)
        val nodeAccessItems = getNodeAccessItemsFromAccessMap(updateAccessRequest.nodeID, nodeWorkspaceID, granterID, updateAccessRequest.userIDToAccessTypeMap)
        nodeRepository.createBatchNodeAccessItem(nodeAccessItems)
    }

    fun updateSharedNode(wdRequest: WDRequest, userID: String) {
        val nodeRequest = wdRequest as UpdateSharedNodeRequest
        require(nodeAccessService.checkIfNodeSharedWithUser(nodeRequest.id, userID, listOf(AccessType.MANAGE, AccessType.WRITE))) { Messages.ERROR_NODE_PERMISSION }
        val storedNode = nodeRepository.getNodeByNodeID(nodeRequest.id, ItemStatus.ACTIVE) ?: throw NoSuchElementException( Messages.INVALID_NODE_ID )
        val node = createNodeObjectFromUpdateShareNodeRequest(nodeRequest, storedNode.workspaceIdentifier.id, storedNode.namespaceIdentifier.id, userID)
        updateNode(node, storedNode)
    }

    fun revokeSharedAccess(wdRequest: WDRequest, revokerID: String, revokerWorkspaceID: String) {
        val sharedNodeRequest = wdRequest as SharedNodeRequest

        // check if the revoker has manage access
        require(nodeAccessService.checkIfUserHasAccess(revokerWorkspaceID, sharedNodeRequest.nodeID, revokerID, EntityOperationType.MANAGE)) { Messages.ERROR_NODE_PERMISSION }

        // since only PK and SK matter here for deletion, can fill dummy fields.
        val nodeAccessItems = getNodeAccessItems(sharedNodeRequest.nodeID, revokerWorkspaceID, revokerID, sharedNodeRequest.userIDs, sharedNodeRequest.accessType)
        nodeRepository.deleteBatchNodeAccessItem(nodeAccessItems)
    }

    /* will return information only if user has MANAGE access to the node or the namespace of the node */
    fun getAllSharedUsersOfNode(nodeID: String, userID: String, userWorkspaceID: String): Map<String, String> {
        require(nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.MANAGE).isNotEmpty()) { Messages.ERROR_NODE_PERMISSION }
        return nodeRepository.getSharedUserInformation(nodeID)
    }

    fun getAccessDataForUser(nodeID: String, userID: String, userWorkspaceID: String): String = runBlocking {
        val nodeWorkspaceNamespacePair = nodeRepository.getNodeWorkspaceAndNamespace(nodeID)
        require(nodeWorkspaceNamespacePair != null) { Messages.INVALID_NODE_ID }

        if (nodeWorkspaceNamespacePair.first == userWorkspaceID) return@runBlocking AccessType.MANAGE.name

        val getNodeAccessTypeJob = async { nodeAccessService.getUserNodeAccessType(nodeID, userID) }
        val getNamespaceAccessTypeJob = async { namespaceService.namespaceAccessService.getUserNamespaceAccessType(nodeWorkspaceNamespacePair.second, userID)}
        val nodeAccessLevel = getNodeAccessTypeJob.await()
        val namespaceAccessLevel = getNamespaceAccessTypeJob.await()

        return@runBlocking when(namespaceAccessLevel.ordinal > nodeAccessLevel.ordinal){
            true -> namespaceAccessLevel.name
            false -> nodeAccessLevel.name
        }
    }

    fun getAllSharedNodesWithUser(userID: String): List<Map<String, String>> {
        val nodeAccessItemsMap = nodeRepository.getAllSharedNodesWithUser(userID)
        return getNodeTitleWithIDs(nodeAccessItemsMap)
    }

    fun getNodeTitleWithIDs(nodeAccessItemsMap: Map<String, NodeAccess>): List<Map<String, String>> {
        val setOfNodeIDWorkspaceID = createSetFromNodeAccessItems(nodeAccessItemsMap.values.toList())
        val unprocessedData = nodeRepository.batchGetNodeMetadataAndTitle(setOfNodeIDWorkspaceID)
        val list = mutableListOf<Map<String, String>>()
        for (nodeData in unprocessedData) {
            populateMapForSharedNodeData(nodeData, nodeAccessItemsMap).let {
                if (it.isNotEmpty()) list.add(it)
            }
        }
        return list
    }

    private fun createSetFromNodeAccessItems(nodeAccessItems: List<NodeAccess>): Set<Pair<String, String>> {
        return nodeAccessItems.map { Pair(it.node.id, it.workspace.id) }.toSet()
    }

    private fun populateMapForSharedNodeData(nodeData: MutableMap<String, AttributeValue>, nodeAccessItemsMap: Map<String, NodeAccess>): Map<String, String> {
        if (nodeData["itemStatus"]!!.s == ItemStatus.ARCHIVED.name) return mapOf() // if the shared node has been archived, don't return data.
        val map = mutableMapOf<String, String>()

        val nodeID = nodeData["SK"]!!.s
        map["nodeID"] = nodeID
        map["nodeTitle"] = nodeData["title"]!!.s
        map["accessType"] = nodeAccessItemsMap[nodeID]!!.accessType.name
        map["granterID"] = nodeAccessItemsMap[nodeID]!!.granterID
        map["ownerID"] = nodeData["createdBy"]!!.s

        val metadata = if (nodeData.containsKey("metadata")) nodeData["metadata"]!!.s else null
        val nodeMetadataJson = """
            {
                "createdAt" : ${nodeData["createdAt"]!!.n} ,
                "updatedAt" : ${nodeData["updatedAt"]!!.n} ,
                "metadata" : $metadata
                
            }
        """.trimIndent()
        map["nodeMetadata"] = nodeMetadataJson

        return map
    }

    fun getMetadataForNodesOfWorkspace(workspaceID: String): Map<String, Map<String, Any?>> {
        return nodeRepository.getMetadataForNodesOfWorkspace(workspaceID)
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }
}