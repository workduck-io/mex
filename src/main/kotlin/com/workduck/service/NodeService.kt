package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.BlockMovementRequest
import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.MetadataRequest
import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodeNamePath
import com.serverless.models.requests.NodePath
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.RefactorRequest
import com.serverless.models.requests.SharedNamespaceRequest
import com.serverless.models.requests.SharedNodeRequest
import com.serverless.models.requests.UpdateAccessTypesRequest
import com.serverless.models.requests.UpdateSharedNodeRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.addAlphanumericStringToTitle
import com.serverless.utils.addIfNotEmpty
import com.serverless.utils.commonPrefixList
import com.serverless.utils.convertToPathString
import com.serverless.utils.createNodePath
import com.serverless.utils.getDifferenceWithOldHierarchy
import com.serverless.utils.getListFromPath
import com.serverless.utils.getRoughSizeOfEntity
import com.serverless.utils.isNodeUnchanged
import com.serverless.utils.mix
import com.serverless.utils.removePrefixList
import com.serverless.utils.CacheHelper
import com.serverless.utils.orderPage
import com.workduck.models.AccessType
import com.workduck.models.AdvancedElement
import com.workduck.models.BlockMovementAction
import com.workduck.models.Entity
import com.workduck.models.EntityOperationType
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Namespace
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.NodeAccess
import com.workduck.models.NodeIdentifier
import com.workduck.models.NodeOperationType
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
import com.workduck.utils.RelationshipHelper.findStartNodeOfEndNode
import com.workduck.utils.TagHelper.createTags
import com.workduck.utils.TagHelper.deleteTags
import com.workduck.utils.TagHelper.updateTags
import com.workduck.utils.WorkspaceHelper.removeRedundantPaths
import com.workduck.utils.extensions.toIDList
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

    val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig),

) {
    private val workspaceService: WorkspaceService = WorkspaceService(nodeService = this)
    val namespaceService: NamespaceService = NamespaceService(nodeService = this)
    private val nodeAccessService: NodeAccessService = NodeAccessService(nodeRepository, namespaceService.namespaceAccessService)

    fun deleteBlockFromNode(blockIDRequest: WDRequest, userWorkspaceID: String, nodeID: String, userID: String) {
        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.WRITE)
        val blockIDList = (blockIDRequest as GenericListRequest).toIDList()
        nodeRepository.getNodeDataOrderByNodeID(nodeID, nodeWorkspaceID).let {
            require(it.containsAll(blockIDList)) { "BlockID(s) Invalid" }
            nodeRepository.deleteBlockAndDataOrderFromNode(blockIDList, nodeWorkspaceID, nodeID, userID, it)
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
            nodeNamePathToAdd.getListFromPath().last().addAlphanumericStringToTitle()
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
                namespaceService.updateNamespaceHierarchy(namespace, nodeHierarchy)
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
                val nodePublicAccessValue = when(targetNamespace.publicAccess){
                    true -> 1
                    false -> null
                }
                /* update namespace for the nodes in lastNodeHierarchy and set public access value of targetNamespace */
                updateNamespaceAndPublicAccessForSuccessorNodes(targetNamespace.id, workspaceID, lastNodeHierarchy, nodePublicAccessValue)
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
                val nodePublicAccessValue = when(targetNamespace.publicAccess){
                    true -> 1
                    false -> null
                }
                renameNodeInNamespaceWithAccessValue(lastNodeID, newNodes.allNodes.last(), userID, workspaceID, newNamespaceID, nodePublicAccessValue)
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
                launch { updateNamespaceHierarchy(targetNamespace, newHierarchyOfSourceNamespace) }
                listOfChangedPaths.add(getMapOfDifferenceOfPaths(newHierarchyOfSourceNamespace, hierarchyOfSourceNamespace, sourceNamespace.id))
            }
            false -> { /* two hierarchies gets affected in this case */

                val updatedSourceNamespaceHierarchy = getUpdatedExistingHierarchy(hierarchyOfSourceNamespace, existingNodes)
                launch { updateNamespaceHierarchy(sourceNamespace, updatedSourceNamespaceHierarchy) }
                listOfChangedPaths.add(getMapOfDifferenceOfPaths(updatedSourceNamespaceHierarchy, hierarchyOfSourceNamespace, sourceNamespace.id))

                val updatedTargetNamespaceHierarchy = getNewHierarchyByAddingRefactoredPath(hierarchyOfTargetNamespace, lastNodeHierarchy, newPathTillLastNode)
                launch { updateNamespaceHierarchy(targetNamespace, updatedTargetNamespaceHierarchy) }
                listOfChangedPaths.add(getMapOfDifferenceOfPaths(updatedTargetNamespaceHierarchy, hierarchyOfTargetNamespace, targetNamespace.id))
            }
        }

        mapOfDifferenceOfPaths[Constants.CHANGED_PATHS] = listOfChangedPaths

        return@runBlocking mapOfDifferenceOfPaths
    }

    private fun updateNamespaceAndPublicAccessForSuccessorNodes(namespaceID: String, workspaceID: String, lastNodeHierarchy: List<String>, publicAccess: Int?){
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
            if (getNamePath(path).getListFromPath().commonPrefixList(existingNodes.allNodes) == existingNodes.allNodes) {
                /* break the connection from last node */
                updatedPaths.addIfNotEmpty(path.getListFromPath().subList(0, path.getListFromPath().indexOf(existingNodes.allNodes.last())).convertToPathString())
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
        return nodePath.getListFromPath().subList(nodePath.getListFromPath(Constants.DELIMITER).indexOf(nodeID) + 1, nodePath.getListFromPath().size).convertToPathString()
    }

    private fun renameNodeInNamespaceWithAccessValue(nodeID: String, newName: String, userID: String, workspaceID: String, namespaceID: String, publicAccess: Int?) {
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

        launch { updateNamespaceHierarchy(namespace, updatedNodeHierarchy) }

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

    private fun updateNamespaceHierarchy(namespace: Namespace, updatedNodeHierarchy: List<String>) {
        namespaceService.updateNamespaceHierarchy(
            namespace,
            updatedNodeHierarchy,
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

        return nodePath.getListFromPath().removePrefixList(longestExistingNamePath.getListFromPath())
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
            (it.orderPage() as Node).also { orderedNode ->
                orderedNode.starred = jobToGetStarredStatus.await()
            }
        }
    }

    /* This endpoint will be used either in a user's own namespace or in a shared namespace */
    fun getNodesInBatch(nodeIDRequest: WDRequest, userWorkspaceID: String, userID: String, namespaceID: String?): List<Node> {
        val nodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()
        val workspaceID = when(namespaceID != null){ /*if namespaceID is null, assume user is making the call in own workspace so no need to check for permission */
            true -> {
                    namespaceService.namespaceAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(
                        namespaceID,
                        userWorkspaceID,
                        userID,
                        EntityOperationType.READ
                    )[Constants.WORKSPACE_ID]!!
            }
            false -> userWorkspaceID
        }

        require(nodeIDList.size < Constants.MAX_NODE_IDS_FOR_BATCH_GET) { "Number of NodeIDs should be lesser than ${Constants.MAX_NODE_IDS_FOR_BATCH_GET}" }
        return nodeRepository.batchGetNodes(nodeIDList, workspaceID).map { unorderedNode ->
            unorderedNode.orderPage() as Node
        }
    }

    private fun validateAndGetNamespaceForUser(workspaceID: String, namespaceID: String, userID: String, operationType: EntityOperationType) : Namespace =
        /* permission for the user would be checked when fetching the namespace */
        namespaceService.getNamespace(workspaceID, namespaceID, userID, operationType).let { namespace ->
            require(namespace != null) { Messages.INVALID_NAMESPACE_ID }
            namespace
        }

    fun archiveNodes(nodeIDRequest: WDRequest, userWorkspaceID: String, namespaceID: String, userID: String): List<String> = runBlocking {
        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        val namespace: Namespace = validateAndGetNamespaceForUser(userWorkspaceID, namespaceID, userID, EntityOperationType.MANAGE)

        val currentActiveHierarchy = namespace.nodeHierarchyInformation

        /* compare old and new active hierarchies */
        NodeHelper.updateHierarchiesInNamespace(namespace, passedNodeIDList, NodeOperationType.ARCHIVE)
        launch { namespaceService.updateNamespace(namespace) }

        val nodeIDsToArchive = getRemovedNodeIDs(currentActiveHierarchy, namespace.nodeHierarchyInformation)
        unarchiveOrArchiveNodesInParallel(nodeIDsToArchive, namespace.workspaceIdentifier.id, ItemStatus.ARCHIVED)

        return@runBlocking nodeIDsToArchive
    }

    fun archiveNodesMiddleware(nodeIDRequest: WDRequest, userWorkspaceID: String, namespaceID: String, userID: String): MutableMap<String, List<String>> = runBlocking {

        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        val namespace: Namespace = validateAndGetNamespaceForUser(userWorkspaceID, namespaceID, userID, EntityOperationType.MANAGE)

        val currentActiveHierarchy = namespace.nodeHierarchyInformation

        NodeHelper.updateHierarchiesInNamespace(namespace, passedNodeIDList, NodeOperationType.ARCHIVE)
        launch { namespaceService.updateNamespace(namespace) }

        /* compare old and new active hierarchies */
        val nodeIDsToArchive = getRemovedNodeIDs(currentActiveHierarchy, namespace.nodeHierarchyInformation)

        unarchiveOrArchiveNodesInParallel(nodeIDsToArchive, namespace.workspaceIdentifier.id, ItemStatus.ARCHIVED)

        /* mapOfArchivedHierarchyAndActiveHierarchyDiff */
        return@runBlocking mutableMapOf(Constants.ARCHIVED_HIERARCHY to namespace.archivedNodeHierarchyInformation).also {
            it.putAll(namespace.nodeHierarchyInformation.getDifferenceWithOldHierarchy(currentActiveHierarchy))
        }
    }

    // TODO( implement the behavior for renaming of nodes while un-archiving in case of clashing names at topmost level )
    fun unarchiveNodesNew(nodeIDRequest: WDRequest, userWorkspaceID: String, namespaceID: String, userID: String): MutableMap<String, List<String>> = runBlocking {
        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()

        val namespace: Namespace = validateAndGetNamespaceForUser(userWorkspaceID, namespaceID, userID, EntityOperationType.MANAGE)

        val currentActiveHierarchy = namespace.nodeHierarchyInformation /* destinationHierarchy */
        val currentArchivedHierarchy = namespace.archivedNodeHierarchyInformation /* sourceHierarchy */

        NodeHelper.updateHierarchiesInNamespace(namespace, passedNodeIDList, NodeOperationType.UNARCHIVE)
        launch { namespaceService.updateNamespace(namespace) }

        /* compare old and new archived hierarchies */
        val nodeIDsToUnarchive = getRemovedNodeIDs(currentArchivedHierarchy, namespace.archivedNodeHierarchyInformation)

        unarchiveOrArchiveNodesInParallel(nodeIDsToUnarchive, namespace.workspaceIdentifier.id, ItemStatus.ACTIVE)

        /* mapOfArchivedHierarchyAndActiveHierarchyDiff */
        return@runBlocking mutableMapOf(Constants.ARCHIVED_HIERARCHY to namespace.archivedNodeHierarchyInformation).also {
            it.putAll(namespace.nodeHierarchyInformation.getDifferenceWithOldHierarchy(currentActiveHierarchy))
        }
    }


    fun deleteArchivedNodes(nodeIDRequest: WDRequest, userWorkspaceID: String, namespaceID: String, userID: String) : List<String> = runBlocking {

        val passedNodeIDList = (nodeIDRequest as GenericListRequest).toNodeIDList()
        val namespace: Namespace = validateAndGetNamespaceForUser(userWorkspaceID, namespaceID, userID, EntityOperationType.MANAGE)
        val workspaceID = namespace.workspaceIdentifier.id

        val currentArchivedHierarchy = namespace.archivedNodeHierarchyInformation

        NodeHelper.updateHierarchiesInNamespace(namespace, passedNodeIDList, NodeOperationType.DELETE)
        launch { namespaceService.updateNamespace(namespace) }

        /* compare old and new archived hierarchies */
        val nodeIDsToDelete = getRemovedNodeIDs(currentArchivedHierarchy, namespace.archivedNodeHierarchyInformation)

        /* only archived nodes are passed */
        softDeleteNodesInParallel(nodeIDsToDelete, workspaceID, userID)
        return@runBlocking nodeIDsToDelete
    }


    fun getRemovedNodeIDs(oldHierarchy: List<String>, newHierarchy: List<String>): List<String> {
        val oldNodeIDs = getNodeIDsFromHierarchy(oldHierarchy)
        val newNodeIDs = getNodeIDsFromHierarchy(newHierarchy)
        return oldNodeIDs.filter { oldNodeID ->
            newNodeIDs.all { it != oldNodeID }
        }
    }

    fun softDeleteNodesInParallel(nodeIDList: List<String>, workspaceID: String, userID: String)  = runBlocking {

        val jobToDelete = CoroutineScope(Dispatchers.IO + Job()).async {
            supervisorScope {
                val deferredList = ArrayList<Deferred<*>>()
                for (nodeID in nodeIDList) {
                    deferredList.add(
                        async {
                            nodeRepository.softDeleteNode(nodeID, workspaceID, userID)
                        })
                    deferredList.joinAll()
                }
            }
        }

        jobToDelete.await()
    }


    fun changeNamespaceOfNodesInParallel(nodeIDList: List<String>, workspaceID: String, sourceNamespaceID: String, targetNamespaceID : String, userID: String)  = runBlocking {

        val jobToDelete = CoroutineScope(Dispatchers.IO + Job()).async {
            supervisorScope {
                val deferredList = ArrayList<Deferred<*>>()
                for (nodeID in nodeIDList) {
                    deferredList.add(
                        async {
                            nodeRepository.changeNamespace(nodeID, workspaceID, sourceNamespaceID, targetNamespaceID, userID)
                        })
                    deferredList.joinAll()
                }
            }
        }

        jobToDelete.await()
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

    private fun updateNamespaceAndPublicAccessOfNodesInParallel(nodeIDList: List<String>, workspaceID: String, namespaceID: String, publicAccess: Int?)  = runBlocking{

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



    /* Getting called Internally via trigger. No need to update hierarchy */
    fun archiveNodes(nodeIDList: List<String>, workspaceID: String) = runBlocking {
        pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ARCHIVED)
    }

    fun getAllNodeIDToNodeNameMap(workspaceID: String, itemStatus: ItemStatus): Map<String, String> {
        return nodeRepository.getAllNodeIDToNodeNameMap(workspaceID, itemStatus)
    }

    fun append(nodeID: String, userWorkspaceID: String, userID: String, elementsListRequest: WDRequest): Map<String, Any>? {

        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.WRITE)
        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val elements = elementsListRequestConverted.elements

        val orderList = mutableListOf<String>()
        for (e in elements) {
            orderList += e.id

            e.lastEditedBy = userID
            e.createdAt = Constants.getCurrentTime()
            e.updatedAt = e.createdAt
        }
        return nodeRepository.append(nodeID, nodeWorkspaceID, userID, elements, orderList)
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
                val idList = getIDPath(nodePath).getListFromPath()
                val indexOfNodeID = idList.indexOf(node.id)
                if (indexOfNodeID != -1) {
                    val nameList = getNamePath(nodePath).getListFromPath().toMutableList()
                    nameList[indexOfNodeID] = node.title
                    newHierarchy.add(nameList.mix(idList).convertToPathString())
                } else {
                    newHierarchy.add(nodePath)
                }
            }
            workspaceService.updateWorkspaceHierarchy(workspace, newHierarchy)
        }
    }

    fun getAllNodesWithWorkspaceID(workspaceID: String): List<String> {
        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithUserID(userID: String): List<String> {
        return nodeRepository.getAllNodesWithUserID(userID)
    }

    /* by default get non deleted nodes only */
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
        val encodedKey = CacheHelper.encodePublicCacheKey(nodeID)
        try {
            val node = publicNodeReadCache.getNode(encodedKey) ?: let {
                val nodeFromDB = pageRepository.getPublicPage(nodeID, Node::class.java).orderPage() as Node
                publicNodeWriteCache.setNode(encodedKey, nodeFromDB)
                return nodeFromDB
            }
            return node.orderPage() as Node
        } finally {
            publicNodeReadCache.closeConnection()
            publicNodeWriteCache.closeConnection()
        }
    }

    fun copyOrMoveBlock(wdRequest: WDRequest, userWorkspaceID: String, userID: String) {

        val copyOrMoveBlockRequest = wdRequest as BlockMovementRequest
        val destinationNodeID = copyOrMoveBlockRequest.destinationNodeID
        val sourceNodeID = copyOrMoveBlockRequest.sourceNodeID

        val destinationNamespaceID = copyOrMoveBlockRequest.destinationNamespaceID
        val sourceNamespaceID = copyOrMoveBlockRequest.sourceNamespaceID
        val blockID = copyOrMoveBlockRequest.blockID

        val workspaceIDOfSourceNode = nodeAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(sourceNodeID, userWorkspaceID, sourceNamespaceID, userID, EntityOperationType.WRITE)[Constants.WORKSPACE_ID] ?: throw IllegalArgumentException(Messages.INVALID_PARAMETERS)

        val workspaceIDOfDestinationNode = nodeAccessService.checkIfUserHasAccessAndGetWorkspaceDetails(destinationNodeID, userWorkspaceID, destinationNamespaceID, userID, EntityOperationType.WRITE)[Constants.WORKSPACE_ID] ?: throw IllegalArgumentException(Messages.INVALID_PARAMETERS)

        val sourceNodeWithBlockAndDataOrder: Node = nodeRepository.getNodeWithBlockAndDataOrder(sourceNodeID, blockID, workspaceIDOfSourceNode).let{ node ->
            require(node != null) { Messages.INVALID_NODE_ID }
            require(node.data?.get(0) != null ) { Messages.INVALID_BLOCK_ID }
            check(!node.dataOrder.isNullOrEmpty()) {Messages.INVALID_NODE_STATE}
            node
        }

        when(copyOrMoveBlockRequest.action){

            BlockMovementAction.COPY -> { sourceNodeWithBlockAndDataOrder.data!![0].also { block ->
                    nodeRepository.append(destinationNodeID, workspaceIDOfDestinationNode, userID, listOf(block), mutableListOf(block.id))
                }
            }

            BlockMovementAction.MOVE -> { sourceNodeWithBlockAndDataOrder.dataOrder!!.also { dataOrder ->
                    dataOrder.remove(blockID)
                    nodeRepository.moveBlock(sourceNodeWithBlockAndDataOrder.data!![0], workspaceIDOfSourceNode, sourceNodeID, workspaceIDOfDestinationNode, destinationNodeID, dataOrder)
                }
            }
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
        return nodeRepository.getNodeByNodeID(nodeID, ItemStatus.ACTIVE)?.orderPage() ?: throw NoSuchElementException(Messages.INVALID_NODE_ID)

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

    fun revokeSharedAccess(wdRequest: WDRequest, revokerUserID: String, revokerWorkspaceID: String) {
        val shareNamespaceRequest = wdRequest as SharedNodeRequest
        val userIDsToRemove = shareNamespaceRequest.userIDs

        when(userIDsToRemove.size){
            1 -> when(userIDsToRemove.first() == revokerUserID){
                true -> revokeOwnAccess(revokerUserID, shareNamespaceRequest.nodeID)
                false -> revokeOthersAccess(userIDsToRemove, revokerUserID, revokerWorkspaceID, shareNamespaceRequest.nodeID)
            }
            else -> revokeOthersAccess(userIDsToRemove, revokerUserID, revokerWorkspaceID, shareNamespaceRequest.nodeID)
        }

    }

    private fun revokeOthersAccess(userIDsToRemove : List<String>, revokerUserID: String, revokerWorkspaceID: String, nodeID: String){
        require(nodeAccessService.checkIfUserHasAccess(revokerWorkspaceID, nodeID, revokerUserID, EntityOperationType.MANAGE)) { Messages.ERROR_NODE_PERMISSION }

        // since PK and SK matter here for deletion, rest can fill dummy fields.
        val nodeAccessItems = getNodeAccessItems(nodeID, revokerWorkspaceID, revokerUserID, userIDsToRemove, AccessType.MANAGE)
        nodeRepository.deleteBatchNodeAccessItem(nodeAccessItems)

    }

    private fun revokeOwnAccess(userID: String, nodeID: String){
        nodeRepository.deleteNodeAccessItem(userID, nodeID)

    }

    /* will return information only if user has MANAGE access to the node or the namespace of the node */
    fun getAllSharedUsersOfNode(nodeID: String, userID: String, userWorkspaceID: String): Map<String, String> = runBlocking {
        require(nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.MANAGE).isNotEmpty()) { Messages.ERROR_NODE_PERMISSION }

        val jobToGetInvitedUsers = async { nodeRepository.getSharedUserInformation(nodeID) }
        val jobToGetNodeOwnerDetails = async { nodeRepository.getOwnerDetailsFromNodeID(nodeID) }

        val mapOfSharedUserDetails = jobToGetInvitedUsers.await().toMutableMap().also {
            it.putAll(jobToGetNodeOwnerDetails.await())
        }

        return@runBlocking mapOfSharedUserDetails
    }

    fun getAccessDataForUser(nodeID: String, userID: String, userWorkspaceID: String): String = runBlocking {
        val nodeWorkspaceNamespacePair = nodeRepository.getNodeWorkspaceAndNamespace(nodeID)
        require(nodeWorkspaceNamespacePair != null) { Messages.INVALID_NODE_ID }

        if (nodeWorkspaceNamespacePair.first == userWorkspaceID) return@runBlocking AccessType.OWNER.name

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

    fun updateMetadataOfNode(wdRequest: WDRequest, nodeID: String, userWorkspaceID: String, userID: String){
        val nodeWorkspaceID = nodeAccessService.checkUserAccessWithoutNamespaceAndReturnWorkspaceID(userWorkspaceID, nodeID, userID, EntityOperationType.WRITE)
        val metadata = (wdRequest as MetadataRequest).pageMetadata
        pageRepository.updateMetadataOfPage(nodeID, nodeWorkspaceID, metadata, userID)
    }


    fun getMetadataForNodesOfWorkspace(workspaceID: String): Map<String, Map<String, Any?>> {
        return nodeRepository.getMetadataForNodesOfWorkspace(workspaceID)
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }
}
