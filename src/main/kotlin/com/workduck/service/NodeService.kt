package com.workduck.service

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
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
import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodePath
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.RefactorRequest
import com.serverless.models.requests.SharedNodeRequest
import com.serverless.models.requests.UpdateAccessTypesRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.addAlphanumericStringToTitle
import com.serverless.utils.addIfNotEmpty
import com.serverless.utils.awaitAndThrowExceptionIfFalse
import com.serverless.utils.convertToPathString
import com.serverless.utils.createNodePath
import com.serverless.utils.getDifferenceWithOldHierarchy
import com.serverless.utils.getListOfNodes
import com.serverless.utils.getNewPath
import com.serverless.utils.getRoughSizeOfEntity
import com.serverless.utils.isNodeAndTagsUnchanged
import com.serverless.utils.mix
import com.serverless.utils.removePrefix
import com.serverless.utils.splitIgnoreEmpty
import com.workduck.models.AccessType
import com.workduck.models.AdvancedElement
import com.workduck.models.Entity
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
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
import com.workduck.utils.AccessItemHelper.getNodeAccessItems
import com.workduck.utils.AccessItemHelper.getNodeAccessItemsFromAccessMap
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.NodeHelper
import com.workduck.utils.NodeHelper.getIDPath
import com.workduck.utils.NodeHelper.getNamePath
import com.workduck.utils.PageHelper.createDataOrderForPage
import com.workduck.utils.PageHelper.mergePageVersions
import com.workduck.utils.PageHelper.orderBlocks
import com.workduck.utils.RelationshipHelper.findStartNodeOfEndNode
import com.workduck.utils.TagHelper.createTags
import com.workduck.utils.TagHelper.deleteTags
import com.workduck.utils.TagHelper.updateTags
import com.workduck.utils.WorkspaceHelper.removeRedundantPaths
import com.workduck.utils.extensions.toNode
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

/**
 * contains all node related logic
 */
class NodeService( // Todo: Inject them from handlers
    val objectMapper: ObjectMapper = Helper.objectMapper,
    val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    val dynamoDB: DynamoDB = DynamoDB(client),
    val mapper: DynamoDBMapper = DynamoDBMapper(client),
    val workspaceService: WorkspaceService = WorkspaceService(),

    var tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

    var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val pageRepository: PageRepository<Node> = PageRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),

    private val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, pageRepository, dynamoDBMapperConfig)

) {

    fun createNode(node: Node, versionEnabled: Boolean): Entity? = runBlocking {
        setMetadataOfNodeToCreate(node)

        launch { createTags(node.tags, node.id, node.workspaceIdentifier.id) }
        return@runBlocking if (versionEnabled) {
            node.lastVersionCreatedAt = node.createdAt
            val nodeVersion: NodeVersion = createNodeVersionFromNode(node)
            node.nodeVersionCount = 1
            nodeRepository.createNodeWithVersion(node, nodeVersion)
        } else {
            repository.create(node)
        }
    }

    private fun createNodeVersionFromNode(node: Node): NodeVersion {
        val nodeVersion = NodeVersion(
            id = "${node.id}${Constants.DELIMITER}VERSION",
            lastEditedBy = node.lastEditedBy,
            createdBy = node.createdBy,
            data = node.data,
            dataOrder = node.dataOrder,
            createdAt = node.createdAt,
            ak = node.ak,
            namespaceIdentifier = node.namespaceIdentifier,
            workspaceIdentifier = node.workspaceIdentifier,
            updatedAt = "UPDATED_AT${Constants.DELIMITER}${node.updatedAt}"
        )
        nodeVersion.version = Helper.generateId("version")
        return nodeVersion
    }

    /* if operation is "create", will be used to create just a single leaf node */
    fun createAndUpdateNode(request: WDRequest?, workspaceID: String, userID: String, versionEnabled: Boolean = false): Entity? =
        runBlocking {

            val nodeRequest: NodeRequest = request as NodeRequest
            val node: Node = createNodeObjectFromNodeRequest(nodeRequest, workspaceID, userID)

            val jobToGetStoredNode = async { getNode(node.id, workspaceID) }
            val jobToGetWorkspace =
                async { node.workspaceIdentifier.id.let { (workspaceService.getWorkspace(it) as Workspace) } }

            return@runBlocking when (val storedNode = jobToGetStoredNode.await()) {
                null -> {
                    node.title =
                        updateNodeHierarchyInSingleCreateAndReturnTitle(
                                nodeRequest.referenceID,
                                node.id,
                                node.title,
                                jobToGetWorkspace.await()
                        )


                    val jobToCreateNode = async { createNode(node, versionEnabled) }
                    jobToCreateNode.await()
                }
                else -> {
                    jobToGetWorkspace.cancel()
                    updateNode(node, storedNode, versionEnabled)
                }
            }
        }

    private fun checkForPathClashAndResolveWithNewTitle(nodeHierarchy : List<String>?, prefixNodePath :String, passedNodeTitle: String) : String {

        val nodeNamePathToAdd = getNamePath(prefixNodePath).createNodePath(passedNodeTitle)

        return nodeHierarchy?.lastOrNull {
            getNamePath(it) == nodeNamePathToAdd
        }?.let {
            nodeNamePathToAdd.getListOfNodes().last().addAlphanumericStringToTitle()
        } ?: passedNodeTitle

    }

    private fun updateNodeHierarchyInSingleCreateAndReturnTitle(
        referenceID: String?,
        nodeID: String,
        passedNodeTitle: String,
        workspace: Workspace
    ): String{

        workspace.nodeHierarchyInformation?.let { NodeHelper.checkForDuplicateNodeID(it, nodeID) }

        var nodeTitle = passedNodeTitle
        when (referenceID) {
            null -> { /* just a single standalone node is created */
                nodeTitle = checkForPathClashAndResolveWithNewTitle(workspace.nodeHierarchyInformation, "", passedNodeTitle)
                workspaceService.addNodePathToHierarchy(workspace.id, "$nodeTitle${Constants.DELIMITER}$nodeID")
                return nodeTitle
            }
            else -> {
                val nodeHierarchy = workspace.nodeHierarchyInformation as MutableList<String>? ?: mutableListOf()
                val nodePathsToAdd = mutableListOf<String>()
                val nodePathsToRemove = mutableListOf<String>()

                for (nodePath in nodeHierarchy) {
                    if (nodePath.endsWith(referenceID)) { /* referenceID was a leaf node */
                        nodeTitle = checkForPathClashAndResolveWithNewTitle(nodeHierarchy, nodePath, passedNodeTitle)
                        nodePathsToAdd.add("$nodePath${Constants.DELIMITER}$nodeTitle${Constants.DELIMITER}$nodeID")

                        if(!NodeHelper.isPathClashing(nodeTitle, passedNodeTitle)) nodePathsToRemove.add(nodePath)
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
                workspaceService.updateWorkspaceHierarchy(workspace, nodeHierarchy, HierarchyUpdateSource.NODE)
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

    fun refactor(wdRequest: WDRequest, userID: String, workspace: Workspace): Map<String, List<String>> = runBlocking {
        val refactorNodePathRequest = wdRequest as RefactorRequest

        /* existingNodePath is path from root till last node in the path and not necessarily path till a leaf node */
        val lastNodeID = refactorNodePathRequest.nodeID

        val existingNodes = refactorNodePathRequest.existingNodePath
        val newNodes = refactorNodePathRequest.newNodePath

        val paths = mutableListOf<String>()
        val nodeHierarchyInformation = workspace.nodeHierarchyInformation as MutableList<String>

        if (newNodes.allNodes.size > 1) addPathsAndCreateNodesBeforeLastNode(refactorNodePathRequest, paths, userID, workspace)

        when (NodeHelper.isRename(existingNodes, newNodes)) {
            true -> { /* need to rename last node from existing path to last node from new path */
                launch { renameNode(lastNodeID, newNodes.allNodes.last(), userID, workspace.id) }
            }
        }

        val renameNodePath = "${newNodes.allNodes.last()}${Constants.DELIMITER}$lastNodeID" /* even if no need to rename, we're good */
        LOG.debug("renameString : $renameNodePath")

        paths.addIfNotEmpty(renameNodePath)

        val combinedPath = paths.joinToString(Constants.DELIMITER)

        /* get paths from lastNode */
        val lastNodeHierarchy = getHierarchyOfNode(nodeHierarchyInformation, lastNodeID)

        val newHierarchy: List<String> = removeRedundantPaths(createNewHierarchyInRefactor(lastNodeHierarchy, nodeHierarchyInformation, combinedPath, existingNodes))

        //removeRedundantPaths(newHierarchy)
        workspaceService.updateWorkspaceHierarchy(workspace, newHierarchy, HierarchyUpdateSource.NODE)
        return@runBlocking newHierarchy.getDifferenceWithOldHierarchy(nodeHierarchyInformation)
    }

    private fun createNewHierarchyInRefactor(lastNodeHierarchy: List<String>, currentHierarchy: List<String>, newPathTillLastNode: String, existingNodes: NodePath): MutableList<String> {

        val newHierarchy = mutableListOf<String>()

        if (lastNodeHierarchy.isEmpty()) newHierarchy.add(newPathTillLastNode)
        else {
            for (lastNodeHierarchyPath in lastNodeHierarchy) {
                newHierarchy.add(newPathTillLastNode.createNodePath(lastNodeHierarchyPath))
            }
        }

        /* collect paths which need to be updated */
        val updatedPaths = mutableListOf<String>()
        for (path in currentHierarchy) {
            if (getNamePath(path).startsWith(existingNodes.path)) {
                /* break the connection from last node */
                updatedPaths.add(path.getListOfNodes().subList(0, path.getListOfNodes().indexOf(existingNodes.allNodes.last())).convertToPathString())
            } else {
                newHierarchy.add(path)
            }
        }

        removeRedundantPaths(updatedPaths, newHierarchy)
        return newHierarchy
    }

    private fun addPathsAndCreateNodesBeforeLastNode(refactorNodePathRequest: RefactorRequest, paths: MutableList<String>, userID: String, workspace: Workspace) {
        val newNodesWithoutLast = NodePath(
            path = refactorNodePathRequest.newNodePath.allNodes.dropLast(1).convertToPathString(),
            namespaceID = refactorNodePathRequest.newNodePath.namespaceID
        )

        val longestExistingPath = NodeHelper.getLongestExistingPath(workspace.nodeHierarchyInformation, newNodesWithoutLast.path)

        paths.addIfNotEmpty(longestExistingPath)

        var listOfNodesToCreate = mutableListOf<Node>()
        try {
            listOfNodesToCreate = setMetaDataForEmptyNodes(getNodesToCreate(longestExistingPath, newNodesWithoutLast).toMutableList(), userID, workspace.id, refactorNodePathRequest.newNodePath.namespaceID)
        } catch (e: IllegalArgumentException) {
            // don't do anything. Just a  case where only renaming has been done, or we're appending to an already existing path
        }

        nodeRepository.createMultipleNodes(listOfNodesToCreate)

        /* path from new nodes to be created (will either be a suffix or an independent string)*/
        val pathForNewNodes = getSuffixPathInBulkCreate(listOfNodesToCreate)

        paths.addIfNotEmpty(pathForNewNodes)
    }

    private fun getHierarchyOfNode(nodeHierarchy: List<String>, nodeID: String): List<String> {
        val listOfPaths = mutableListOf<String>()
        for (nodePath in nodeHierarchy) {
            if (nodePath.contains(nodeID)) {
                listOfPaths.addIfNotEmpty(getPathAfterNode(nodePath, nodeID))
            }
        }
        return listOfPaths
    }

    private fun getPathAfterNode(nodePath: String, nodeID: String): String {
        return nodePath.getListOfNodes().subList(nodePath.getListOfNodes(Constants.DELIMITER).indexOf(nodeID) + 1, nodePath.getListOfNodes().size).convertToPathString()
    }

    private fun renameNode(nodeID: String, newName: String, userID: String, workspaceID: String) {
        nodeRepository.renameNode(nodeID, newName, userID, workspaceID)
    }

    private fun setMetaDataForEmptyNodes(
        namesOfNodesToCreate: MutableList<String>,
        lastEditedBy: String,
        workspaceID: String,
        namespaceID: String?
    ): MutableList<Node> {
        val listOfNodes = mutableListOf<Node>()
        for (newNodeName in namesOfNodesToCreate) {
            listOfNodes.add(
                Node(
                    id = Helper.generateNanoID(IdentifierType.NODE.name),
                    title = newNodeName,
                    workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                    namespaceIdentifier = namespaceID?.let { NamespaceIdentifier(it) },
                    createdBy = lastEditedBy,
                    lastEditedBy = lastEditedBy,
                    data = listOf()
                )
            )
        }

        return listOfNodes
    }

    fun bulkCreateNodes(request: WDRequest, workspaceID: String, userID: String): Map<String, List<String>> = runBlocking {
        val nodeRequest: NodeBulkRequest = request as NodeBulkRequest

        val nodePath: NodePath = nodeRequest.nodePath
        val node: Node = createNodeObjectFromNodeBulkRequest(nodeRequest, workspaceID, userID)
        val workspace: Workspace = workspaceService.getWorkspace(workspaceID) as Workspace

        val nodeHierarchyInformation = (workspace.nodeHierarchyInformation as MutableList<String>)

        NodeHelper.checkForDuplicateNodeID(nodeHierarchyInformation, node.id)

        val longestExistingPath = checkForSamePath(NodeHelper.getLongestExistingPath(nodeHierarchyInformation, nodePath.path), nodePath, node)

        val listOfNodes = getListOfNodesToCreateInBulkCreate(nodePath, longestExistingPath, node)

        /* path from new nodes to be created (will either be a suffix or an independent string)*/
        val suffixNodePath = getSuffixPathInBulkCreate(listOfNodes)

        val updatedNodeHierarchy =
            getUpdatedNodeHierarchyInformation(nodeHierarchyInformation, longestExistingPath, suffixNodePath)

        launch { nodeRepository.createMultipleNodes(listOfNodes) }
        launch {
            workspaceService.updateWorkspaceHierarchy(
                workspace,
                updatedNodeHierarchy,
                HierarchyUpdateSource.NODE
            )
        }
        val mapOfNodeAndDifference = mutableMapOf("node" to listOf(Helper.objectMapper.writeValueAsString(node))) /* requirement of middleware */
        mapOfNodeAndDifference.putAll(updatedNodeHierarchy.getDifferenceWithOldHierarchy(nodeHierarchyInformation)).let {
            mapOfNodeAndDifference
        }
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

    private fun checkForSamePath(longestExistingNamePath: String, nodePath: NodePath, node: Node) : String{
        return if (getNamePath(longestExistingNamePath) == nodePath.path) {
            node.title = nodePath.allNodes.last().addAlphanumericStringToTitle()
            nodePath.allNodes[nodePath.allNodes.lastIndex] = node.title
            nodePath.path = nodePath.allNodes.convertToPathString()
            longestExistingNamePath.getListOfNodes().dropLast(2).convertToPathString()
        } else longestExistingNamePath

    }

    private fun getListOfNodesToCreateInBulkCreate(nodePath: NodePath, longestExistingPath: String, node: Node): List<Node> {

        val nodesToCreate: List<String> = getNodesToCreate(longestExistingPath, nodePath)

        setMetadataOfNodeToCreate(node)
        return setMetaDataFromNode(node, nodesToCreate)
    }

    private fun getNodesToCreate(longestExistingPath: String, nodePath: NodePath): List<String> {
        val longestExistingNamePath = getNamePath(longestExistingPath)

        if (longestExistingNamePath == nodePath.path) { // rename last node
            val newTitle = nodePath.allNodes.last().addAlphanumericStringToTitle()
            nodePath.allNodes[nodePath.allNodes.lastIndex] = newTitle
            nodePath.path = nodePath.allNodes.convertToPathString()
            return listOf(newTitle)
        }

        return nodePath.removePrefix(longestExistingNamePath).splitIgnoreEmpty(Constants.DELIMITER)
    }

    private fun getUpdatedNodeHierarchyInformation(
        oldHierarchy: MutableList<String>,
        longestExistingPath: String,
        suffixNodePath: String
    ): List<String> {

        val newHierarchy = oldHierarchy.toMutableList()

        var nodePathToRemove: String? = null

        for (existingNodePath in oldHierarchy) {
            if (longestExistingPath == existingNodePath) {
                nodePathToRemove = existingNodePath
                break
            }
        }

        /* if nodePathToRemove != null , we are adding to a leaf path, so we remove that path*/
        when (nodePathToRemove != null) {
            true -> newHierarchy.remove(nodePathToRemove)
        }

        newHierarchy.add(longestExistingPath.getNewPath(suffixNodePath))

        return newHierarchy
    }

    fun setMetaDataFromNode(node: Node, nodesToCreate: List<String>?): List<Node> {

        val listOfNodes = mutableListOf<Node>()
        nodesToCreate?.let {
            for (index in 0 until nodesToCreate.size - 1) { /* since the last element is the node itself */
                listOfNodes.add(createEmptyNodeWithMetadata(node, nodesToCreate[index]))
            }
        }

        listOfNodes.add(node)

        return listOfNodes
    }

    private fun createEmptyNodeWithMetadata(node: Node, newNodeName: String): Node {
        val newNode = Node(
            id = Helper.generateNanoID(IdentifierType.NODE.name),
            title = newNodeName,
            workspaceIdentifier = node.workspaceIdentifier,
            namespaceIdentifier = node.namespaceIdentifier,
            createdBy = node.createdBy,
            lastEditedBy = node.lastEditedBy,
            createdAt = node.createdAt,
            ak = "${node.workspaceIdentifier.id}${Constants.DELIMITER}${node.namespaceIdentifier?.id}",
            data = listOf()
        )

        newNode.updatedAt = node.updatedAt

        return newNode
    }

    /* sets AK, dataOrder, createdBy and accountability data of blocks of the node */
    private fun setMetadataOfNodeToCreate(node: Node) {

        node.ak = "${node.workspaceIdentifier.id}${Constants.DELIMITER}${node.namespaceIdentifier?.id}"
        node.dataOrder = createDataOrderForNode(node)

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

    fun getNode(nodeID: String, workspaceID: String, bookmarkInfo: Boolean? = null, userID: String? = null): Node? =
        (
            (
                pageRepository
                    .get(WorkspaceIdentifier(workspaceID), NodeIdentifier(nodeID), Node::class.java)
                )?.let { node -> orderBlocks(node) } as Node?
            )
            ?.also { node ->
                if (userID != null && bookmarkInfo == true)
                    node.isBookmarked = UserBookmarkService().isNodeBookmarkedForUser(nodeID, userID)
            }

    fun archiveNodes(nodeIDRequest: WDRequest, workspaceID: String): MutableList<String> = runBlocking {

        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)

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

    /* Getting called Internally via trigger. No need to update hierarchy */
    fun archiveNodes(nodeIDList: List<String>, workspaceID: String) = runBlocking {
        pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ARCHIVED)
    }

    fun convertGenericRequestToList(genericRequest: GenericListRequest): List<String> {
        return genericRequest.ids
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

    fun updateNode(node: Node, storedNode: Node, versionEnabled: Boolean): Entity? = runBlocking {

        Page.populatePageWithCreatedAndPublicFields(node, storedNode)

        node.dataOrder = createDataOrderForPage(node)

        if (node.isNodeAndTagsUnchanged(storedNode)) {
            return@runBlocking storedNode
        }

        /* to make the locking versions same */
        mergePageVersions(node, storedNode)

        launch { updateHierarchyIfRename(node, storedNode) }

        launch { updateTags(node.tags, storedNode.tags, node.id, node.workspaceIdentifier.id) }
        if (versionEnabled) {
            /* if the time diff b/w the latest version ( in version table ) and current node's updatedAt is < 5 minutes, don't create another version */
            if (node.updatedAt - storedNode.lastVersionCreatedAt!! < 300000) {
                node.lastVersionCreatedAt = storedNode.lastVersionCreatedAt
                return@runBlocking repository.update(node)
            }
            node.lastVersionCreatedAt = node.updatedAt
            checkNodeVersionCount(node, storedNode.nodeVersionCount)

            val nodeVersion = createNodeVersionFromNode(node)
            nodeVersion.createdAt = storedNode.createdAt
            nodeVersion.createdBy = storedNode.createdBy

            return@runBlocking nodeRepository.updateNodeWithVersion(node, nodeVersion)
        } else {
            return@runBlocking repository.update(node)
        }
    }

    private fun updateHierarchyIfRename(node: Node, storedNode: Node) {
        val newHierarchy = mutableListOf<String>()
        if (node.title != storedNode.title) {
            val workspace = workspaceService.getWorkspace(node.workspaceIdentifier.id) as Workspace
            val currentHierarchy = workspace.nodeHierarchyInformation ?: listOf()
            for (nodePath in currentHierarchy) {
                val idList = getIDPath(nodePath).getListOfNodes()
                val indexOfNodeID = idList.indexOf(node.id)
                if(indexOfNodeID != -1){
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

    fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String>? {

        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)
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
            if(it.getRoughSizeOfEntity() > Constants.DDB_MAX_ITEM_SIZE)  throw WDNodeSizeLargeException("Node size is too large")
        }

    private fun createNodeObjectFromNodeBulkRequest(nodeBulkRequest: NodeBulkRequest, workspaceID: String, userID: String): Node =
        nodeBulkRequest.toNode(workspaceID, userID)

    fun getAllArchivedSnippetIDsOfWorkspace(workspaceID: String): MutableList<String> {
        return pageRepository.getAllArchivedPagesOfWorkspace(workspaceID, ItemType.Node)
    }

    fun unarchiveNodes(nodeIDRequest: WDRequest, workspaceID: String): List<String> = runBlocking {
        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest) as MutableList
        val mapOfNodeIDToName = getArchivedNodesToRename(nodeIDList, workspaceID)

        LOG.debug(mapOfNodeIDToName)
        for ((nodeID, _) in mapOfNodeIDToName) {
            nodeIDList.remove(nodeID)
        }

        val jobToUnarchiveAndRenameNodes = async { nodeRepository.unarchiveAndRenameNodes(mapOfNodeIDToName, workspaceID) }

        val jobToUnarchiveNodes =
            async { pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ACTIVE) }

        return@runBlocking jobToUnarchiveAndRenameNodes.await() + jobToUnarchiveNodes.await()
    }

    /* this is called internally via trigger. We don't need to do sanity check for name here */
    fun unarchiveNodes(nodeIDList: List<String>, workspaceID: String): MutableList<String> {
        return pageRepository.unarchiveOrArchivePages(nodeIDList, workspaceID, ItemStatus.ACTIVE)
    }

    /* to ensure that nodes at same level don't have same name */
    private fun getArchivedNodesToRename(nodeIDList: List<String>, workspaceID: String): Map<String, String> =
        runBlocking {

            val jobToGetWorkspace = async { workspaceService.getWorkspace(workspaceID) as Workspace }

            val jobToGetArchivedNodeIDToNameMap = async { getAllNodeIDToNodeNameMap(workspaceID, ItemStatus.ARCHIVED) }
            val jobToGetArchivedHierarchyRelationship =
                async { RelationshipService().getHierarchyRelationshipsOfWorkspace(workspaceID, ItemStatus.ARCHIVED) }

            val nodeHierarchyInformation = jobToGetWorkspace.await().nodeHierarchyInformation ?: listOf()
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

        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)
        require(getAllArchivedSnippetIDsOfWorkspace(workspaceID).sorted() == nodeIDList.sorted()) { "The passed IDs should be present and archived" }
        val deletedNodesList: MutableList<String> = mutableListOf()
        for (nodeID in nodeIDList) {
            val tags = nodeRepository.getTags(nodeID, workspaceID)
            if (!tags.isNullOrEmpty()) launch { deleteTags(tags, nodeID, workspaceID) }
            repository.delete(WorkspaceIdentifier(workspaceID), NodeIdentifier(nodeID))?.also {
                deletedNodesList.add(it.id)
            }
        }
        return@runBlocking deletedNodesList
    }

    fun makeNodePublic(nodeID: String, workspaceID: String) {
        pageRepository.togglePagePublicAccess(nodeID, workspaceID, 1)
    }

    fun makeNodePrivate(nodeID: String, workspaceID: String) {
        pageRepository.togglePagePublicAccess(nodeID, workspaceID, 0)
    }

    fun getPublicNode(nodeID: String, workspaceID: String): Node {
        return pageRepository.getPublicPage(nodeID, Node::class.java)
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

        val jobToGetSourceNodeWorkspaceID = async { checkIfNodeExistsForWorkspace(sourceNodeID, workspaceID) }
        val jobToGetDestinationNodeWorkspaceID = async { checkIfNodeExistsForWorkspace(destinationNodeID, workspaceID) }

        jobToGetSourceNodeWorkspaceID.awaitAndThrowExceptionIfFalse(jobToGetDestinationNodeWorkspaceID, Messages.NODE_IDS_DO_NOT_EXIST)
    }

    private fun checkIfNodeExistsForWorkspace(nodeID: String, workspaceID: String): Boolean {
        return nodeRepository.checkIfNodeExistsForWorkspace(nodeID, workspaceID)
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

    fun shareNode(wdRequest: WDRequest, granterID: String, workspaceID: String) {
        val sharedNodeRequest = wdRequest as SharedNodeRequest
        val userIDs = getUserIDsWithoutGranterID(sharedNodeRequest.userIDs, granterID)

        if (userIDs.isEmpty()) return

        val nodeWorkspaceDetails = checkIfOwnerCanManageAndGetWorkspaceDetails(sharedNodeRequest.nodeID, workspaceID, granterID)
        val nodeAccessItems = getNodeAccessItems(sharedNodeRequest.nodeID, nodeWorkspaceDetails["workspaceID"]!!, nodeWorkspaceDetails["workspaceOwner"]!!, granterID, userIDs, sharedNodeRequest.accessType)
        nodeRepository.createBatchNodeAccessItem(nodeAccessItems)
    }

    private fun getUserIDsWithoutGranterID(userIDs: List<String>, ownerID: String): List<String> {
        return userIDs.filter { id -> id != ownerID }
    }

    private fun checkIfOwnerCanManageAndGetWorkspaceDetails(nodeID: String, workspaceID: String, granterID: String): Map<String, String> {
        var isNodeInCurrentWorkspace = false

        if (checkIfNodeExistsForWorkspace(nodeID, workspaceID)) isNodeInCurrentWorkspace = true
        else if (!nodeRepository.getUserIDsWithNodeAccess(nodeID, listOf(AccessType.MANAGE)).contains(granterID)) {
            throw NoSuchElementException("Node you're trying to share does not exist")
        }

        val workspaceDetailsMap = mutableMapOf<String, String>()

        return when (isNodeInCurrentWorkspace) {
            true -> {
                workspaceDetailsMap["workspaceID"] = workspaceID
                workspaceDetailsMap["workspaceOwner"] = granterID
                workspaceDetailsMap
            }
            false -> {
                nodeRepository.getNodeWorkspaceIDAndOwner(nodeID)
            }
        }
    }

    fun getSharedNode(nodeID: String, userID: String): Entity {
        require(nodeRepository.checkIfAccessRecordExists(nodeID, userID)) { "Error Accessing Node" }
        return orderBlocks(nodeRepository.getNodeByNodeID(nodeID))
    }

    fun changeAccessType(wdRequest: WDRequest, granterID: String, workspaceID: String) {
        val updateAccessRequest = wdRequest as UpdateAccessTypesRequest
        val nodeWorkspaceDetails = checkIfOwnerCanManageAndGetWorkspaceDetails(updateAccessRequest.nodeID, workspaceID, granterID)
        val nodeAccessItems = getNodeAccessItemsFromAccessMap(updateAccessRequest.nodeID, nodeWorkspaceDetails["workspaceID"]!!, nodeWorkspaceDetails["workspaceOwner"]!!, granterID, updateAccessRequest.userIDToAccessTypeMap)
        nodeRepository.createBatchNodeAccessItem(nodeAccessItems)
    }

    private fun checkIfGranterCanManage(granterID: String, workspaceID: String, nodeID: String): Boolean {
        return checkIfNodeExistsForWorkspace(nodeID, workspaceID) ||
            nodeRepository.getUserIDsWithNodeAccess(nodeID, listOf(AccessType.MANAGE)).contains(granterID)
    }

    private fun checkIfUserHasAccess(ownerID: String, workspaceID: String, nodeID: String): Boolean {
        return checkIfNodeExistsForWorkspace(nodeID, workspaceID) ||
                nodeRepository.getUserIDsWithNodeAccess(nodeID, AccessType.values().toList()).contains(ownerID)
    }

    private fun checkIfUserHasWriteAccessOrOwner(nodeID: String, userID: String, workspaceID: String) : Boolean {
        return  checkIfNodeExistsForWorkspace(nodeID, workspaceID) || nodeRepository.getUserIDsWithNodeAccess(nodeID, listOf(AccessType.MANAGE, AccessType.WRITE)).contains(userID)
    }

    fun updateSharedNode(wdRequest: WDRequest, userID: String): Entity? {
        val nodeRequest = wdRequest as NodeRequest
        require(nodeRepository.getUserIDsWithNodeAccess(nodeRequest.id, listOf(AccessType.MANAGE, AccessType.WRITE)).contains(userID)) { "Error Accessing Node" }
        val storedNode = nodeRepository.getNodeByNodeID(nodeRequest.id)
        val node = createNodeObjectFromNodeRequest(nodeRequest, storedNode.workspaceIdentifier.id, userID)
        return updateNode(node, storedNode, false)
    }

    fun revokeSharedAccess(wdRequest: WDRequest, ownerID: String, workspaceID: String) {
        val sharedNodeRequest = wdRequest as SharedNodeRequest
        if (!checkIfGranterCanManage(ownerID, workspaceID, sharedNodeRequest.nodeID)) throw NoSuchElementException("Node you're trying to share does not exist")

        // since PK and SK matter here for deletion, can fill dummy fields.
        val nodeAccessItems = getNodeAccessItems(sharedNodeRequest.nodeID, workspaceID, ownerID, ownerID, sharedNodeRequest.userIDs, sharedNodeRequest.accessType)
        nodeRepository.deleteBatchNodeAccessItem(nodeAccessItems)
    }

    fun getAllSharedUsersOfNode(nodeID: String, userID: String, workspaceID: String) : Map<String, String>{
        if(!checkIfUserHasWriteAccessOrOwner(nodeID, userID, workspaceID)) throw NoSuchElementException("Not Found")
        return nodeRepository.getSharedUserInformation(nodeID)
    }

    fun getAllSharedNodesWithUser(userID: String): List<Map<String, String>> {
        val nodeAccessItemsMap = nodeRepository.getAllSharedNodesWithUser(userID)
        return getNodeTitleWithIDs(nodeAccessItemsMap)
    }

    fun getNodeTitleWithIDs(nodeAccessItemsMap: Map<String, NodeAccess>): List<Map<String, String>> {
        val setOfNodeIDWorkspaceID = createSetFromNodeAccessItems(nodeAccessItemsMap.values.toList())
        val unprocessedData = nodeRepository.batchGetNodeTitle(setOfNodeIDWorkspaceID)
        val list = mutableListOf<Map<String, String>>()
        for (nodeData in unprocessedData) {
            populateMapForSharedNodeData(nodeData, nodeAccessItemsMap).let {
                if(it.isNotEmpty()) list.add(it)
            }
        }
        return list
    }

    private fun createSetFromNodeAccessItems(nodeAccessItems: List<NodeAccess>): Set<Pair<String, String>> {
        return nodeAccessItems.map { Pair(it.node.id, it.workspace.id) }.toSet()
    }

    private fun populateMapForSharedNodeData(nodeData: MutableMap<String, AttributeValue>, nodeAccessItemsMap: Map<String, NodeAccess>): Map<String, String> {
        if( nodeData["itemStatus"]!!.s == ItemStatus.ARCHIVED.name ) return mapOf()
        val map = mutableMapOf<String, String>()

        val nodeID = nodeData["SK"]!!.s
        map["nodeID"] = nodeID
        map["nodeTitle"] = nodeData["title"]!!.s
        map["accessType"] = nodeAccessItemsMap[nodeID]!!.accessType.name
        map["granterID"] = nodeAccessItemsMap[nodeID]!!.granterID
        map["ownerID"] = nodeAccessItemsMap[nodeID]!!.ownerID

        return map
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }

}