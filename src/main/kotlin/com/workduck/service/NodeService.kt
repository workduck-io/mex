package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.serverless.internalTagHandlers.TagInput
import com.serverless.models.Input
import com.serverless.models.requests.BlockMovementRequest
import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NodeBulkRequest
import com.serverless.models.requests.NodePath
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.RefactorRequest
 import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.commonPrefixList
import com.serverless.utils.containsExistingNodes
import com.serverless.utils.convertToPathString
import com.serverless.utils.createNodePath
import com.serverless.utils.getDifferenceWithOldHierarchy
import com.serverless.utils.getListOfNodes
import com.serverless.utils.getNodesAfterIndex
import com.serverless.utils.isSingleNodePassed
import com.serverless.utils.mix
import com.serverless.utils.removePrefix
import com.serverless.utils.splitIgnoreEmpty
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.NodeIdentifier
import com.workduck.models.Page
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.PageRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.NodeHelper
import com.workduck.utils.NodeHelper.getCommonPrefixNodePath
import com.workduck.utils.NodeHelper.getIDPath
import com.workduck.utils.NodeHelper.getNamePath
import com.workduck.utils.RelationshipHelper.findStartNodeOfEndNode
import com.workduck.utils.PageHelper.comparePageWithStoredPage
import com.workduck.utils.PageHelper.createDataOrderForPage
import com.workduck.utils.PageHelper.mergePageVersions
import com.workduck.utils.PageHelper.orderBlocks
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import com.serverless.utils.toNode
import com.workduck.models.AdvancedElement
import com.workduck.models.Entity
import com.workduck.models.ItemType
import com.workduck.models.NodeVersion
import com.workduck.utils.NodeHelper.isExistingPathDividedInRefactor
import com.workduck.utils.NodeHelper.isNodeIDInPath
import com.workduck.utils.NodeHelper.removeRedundantPaths
import kotlinx.coroutines.Deferred
import com.workduck.utils.TagHelper.createTags
import com.workduck.utils.TagHelper.deleteTags
import com.workduck.utils.TagHelper.updateTags
import org.apache.logging.log4j.core.tools.picocli.CommandLine

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


    fun createNode(node: Node, versionEnabled: Boolean): Entity? = runBlocking{
        LOG.info("Should be created in the table : $tableName")

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

            val jobToGetStoredNode = async { getNode(node.id, workspaceID) as Node? }
            val jobToGetWorkspace =
                async { node.workspaceIdentifier.id.let { (workspaceService.getWorkspace(it) as Workspace) } }

            return@runBlocking when (val storedNode = jobToGetStoredNode.await()) {
                null -> {
                    val jobToCreateNode = async { createNode(node, versionEnabled) }
                    val workspace = jobToGetWorkspace.await()
                    launch {
                        updateNodeHierarchyInSingleCreate(
                            nodeRequest.referenceID,
                            node.id,
                            node.title,
                            workspace
                        )
                    }
                    jobToCreateNode.await()
                }
                else -> {
                    jobToGetWorkspace.cancel()
                    updateNode(node, storedNode, versionEnabled)
                }
            }
        }

    private fun updateNodeHierarchyInSingleCreate(
        referenceID: String?,
        nodeID: String,
        nodeTitle: String,
        workspace: Workspace
    ) {
        when (referenceID) {
            null -> { /* just a single standalone node is created */
                workspaceService.addNodePathToHierarchy(workspace.id, "$nodeTitle${Constants.DELIMITER}$nodeID")
            }
            else -> {
                val nodeHierarchy = workspace.nodeHierarchyInformation as MutableList<String>? ?: mutableListOf()
                val nodePathsToAdd = mutableListOf<String>()
                val nodePathsToRemove = mutableListOf<String>()

                for (nodePath in nodeHierarchy) {
                    if (nodePath.endsWith(referenceID)) { /* referenceID was a leaf node */
                        nodePathsToAdd.add("$nodePath${Constants.DELIMITER}$nodeTitle${Constants.DELIMITER}$nodeID")
                        nodePathsToRemove.add(nodePath)
                    } else if (nodePath.contains(referenceID)) { /* referenceID is not leaf node, and we need to extract path till referenceID */
                        val endIndex = nodePath.indexOf(referenceID) + referenceID.length - 1
                        val pathTillRefID = nodePath.substring(0, endIndex + 1)
                        nodePathsToAdd.add("$pathTillRefID${Constants.DELIMITER}$nodeTitle${Constants.DELIMITER}$nodeID")
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
    }

    /**
     * Supports movements within namespace only
     * will be used to insert empty nodes in between two existing nodes
     *
     */
    fun refactor(wdRequest: WDRequest, userID: String, workspace: Workspace): Map<String, List<String>> = runBlocking {

        val refactorNodePathRequest = wdRequest as RefactorRequest

        /* existingNodePath is path from root till last node in the path and not necessarily path till a leaf node */
        val lastNodeID = refactorNodePathRequest.nodeID

        val existingNodes = refactorNodePathRequest.existingNodePath.allNodes
        val newNodes = refactorNodePathRequest.newNodePath.allNodes

        // Data model has ensures that list will never be empty
        when (existingNodes.last() != newNodes.last()) {
            true -> { /* need to rename last node from existing path to last node from new path */
                launch { renameNode(lastNodeID, newNodes.last(), userID, workspace.id) }
            }
        }

        val namesOfNodesToCreate = getNodesToCreateInRefactor(existingNodes, newNodes)

        val nodesToCreate: List<Node> = setMetaDataForEmptyNodes(namesOfNodesToCreate, userID, workspace.id, refactorNodePathRequest.namespaceID)

        launch { nodeRepository.createMultipleNodes(nodesToCreate) }

        val unchangedNodes = existingNodes.commonPrefixList(newNodes) as MutableList

        return@runBlocking updateHierarchyInRefactor(unchangedNodes, newNodes, workspace, nodesToCreate, lastNodeID, existingNodes)
    }

    private fun getNodesToCreateInRefactor(existingNodes : List<String>, newNodes : List<String>) : MutableList<String>{
        val namesOfNodesToCreate = newNodes.minus(existingNodes.toSet()).toMutableList()
        /* since the last node just needs renaming at max, we don't need to create it again */
        namesOfNodesToCreate.remove(newNodes.last())
        return namesOfNodesToCreate
    }


    private fun handleSingleNodeInRefactor(workspace: Workspace, lastNodeID : String, newNodeName: String, existingNodeName: String) : Map<String, List<String>>{
        val oldHierarchy = workspace.nodeHierarchyInformation?.toMutableList() ?: throw IllegalArgumentException("Nodes supplied are invalid")
        val newHierarchy = mutableListOf<String>()
        val existingSingleNodePath = existingNodeName.createNodePath(lastNodeID)
        for(nodePath in oldHierarchy){
            if(nodePath.startsWith(existingSingleNodePath)) {
                val suffixNodePath = nodePath.splitIgnoreEmpty(Constants.DELIMITER).toMutableList().drop(2).convertToPathString()
                newHierarchy.add(newNodeName.createNodePath(lastNodeID).createNodePath(suffixNodePath))
            }
            else newHierarchy.add(nodePath)

        }
        workspaceService.updateWorkspaceHierarchy(workspace, newHierarchy, HierarchyUpdateSource.RENAME)
        return newHierarchy.getDifferenceWithOldHierarchy(oldHierarchy)
    }

    private fun updateHierarchyInRefactor(
            unchangedNodes: List<String>,
            newNodes: List<String>,
            workspace: Workspace,
            nodesToCreate: List<Node>,
            lastNodeID: String,
            existingNodes: List<String>,
    ) : Map<String, List<String>> {



        val nodeHierarchyInformation =
            workspace.nodeHierarchyInformation ?: throw NullPointerException("No Hierarchy Found")
        var newNodeHierarchy = mutableListOf<String>()
        var nodePathWithIDsOfExistingNodes = ""

        if(newNodes.isSingleNodePassed(existingNodes)) {
            return handleSingleNodeInRefactor(workspace, lastNodeID, newNodes.first(), existingNodes.first())
        }

        for (nodePath in nodeHierarchyInformation) {
            val namePath = getNamePath(nodePath)
            if(namePath.containsExistingNodes(existingNodes)) nodePathWithIDsOfExistingNodes = nodePath

            /* if the current nodeNamePath has all the nodes from the passed path, we know we need to change this current path */
            if (getCommonPrefixNodePath(unchangedNodes.convertToPathString(), namePath).split(Constants.DELIMITER) == unchangedNodes
                    && nodePath.contains(lastNodeID)) {

                LOG.info("UNCHANGED NODE PATH : ${unchangedNodes.convertToPathString()}, CURRENT NAME PATH : $namePath")

                /* break new node path in 4 parts and combine later
                1. Unchanged Prefix Path
                2. Path due to new Nodes in between
                3. Path due to renaming of one node
                4. Unchanged Suffix Path
                 */

                val paths = mutableListOf<String>()

                val idPath = getIDPath(nodePath).split(Constants.DELIMITER)
                LOG.info("ID PATH : $idPath")

                val idsOfUnchangedPrefixNodes = idPath.subList(0, unchangedNodes.size)
                val prefixString =
                        unchangedNodes.zip(idsOfUnchangedPrefixNodes) { name, id -> "$name${Constants.DELIMITER}$id" }
                                .joinToString(Constants.DELIMITER)

                LOG.info("Prefix String : $prefixString")
                paths.add(prefixString)

                // path due to potential new nodes in between
                val newString = nodesToCreate.joinToString(Constants.DELIMITER) { node -> "${node.title}${Constants.DELIMITER}${node.id}" }
                LOG.info("newString : $newString")
                paths.add(newString)

                // rename string
                val renameNodePath = "${newNodes.last()}${Constants.DELIMITER}$lastNodeID"  /* even if no need to rename, we're good */
                LOG.info("renameString : $renameNodePath")
                paths.add(renameNodePath)

                // suffix string
                val idsOfUnchangedSuffixNodes = idPath.getNodesAfterIndex(idPath.indexOf(lastNodeID))
                val namesOfUnchangedSuffixNodes = namePath.getListOfNodes().takeLast(idsOfUnchangedSuffixNodes.size) /* get all the nodes after last node from passed existing path */

                val suffixString =
                    namesOfUnchangedSuffixNodes.zip(idsOfUnchangedSuffixNodes) { name, id -> "$name${Constants.DELIMITER}$id" }
                        .joinToString(Constants.DELIMITER)
                LOG.info("suffixString : $suffixString")
                paths.add(suffixString)

                newNodeHierarchy.add(paths.joinToString(Constants.DELIMITER))
                LOG.info("New path : " + newNodeHierarchy.last())
            } else {
                newNodeHierarchy.add(nodePath)
            }
        }

        if(isExistingPathDividedInRefactor(unchangedNodes, existingNodes)){
            newNodeHierarchy = removeRedundantPaths(listOf(getFirstPath(nodePathWithIDsOfExistingNodes, lastNodeID)), newNodeHierarchy) as MutableList<String>
        }

        LOG.debug(newNodeHierarchy)
        workspaceService.updateWorkspaceHierarchy(workspace, newNodeHierarchy, HierarchyUpdateSource.NODE)
        return newNodeHierarchy.getDifferenceWithOldHierarchy(nodeHierarchyInformation)

    }

    private fun getFirstPath(nodePathWithIDsOfExistingNodes: String, lastNodeID: String): String{
        val indexOfLastNode = nodePathWithIDsOfExistingNodes.getListOfNodes().indexOf(lastNodeID)
        return nodePathWithIDsOfExistingNodes.getListOfNodes().take(indexOfLastNode-1).convertToPathString()
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


    fun bulkCreateNodes(request: WDRequest, workspaceID: String, userID: String) : Map<String, List<String>> = runBlocking {
        val nodeRequest: NodeBulkRequest = request as NodeBulkRequest

        val node: Node = createNodeObjectFromNodeRequest(nodeRequest, workspaceID, userID)

        val workspace: Workspace = workspaceService.getWorkspace(workspaceID) as Workspace

        val nodeHierarchyInformation = workspace.nodeHierarchyInformation as MutableList<String>

        val nodePath: NodePath = nodeRequest.nodePath

        val longestExistingPath = NodeHelper.getLongestExistingPath(nodeHierarchyInformation, nodePath.path)

        val longestExistingNamePath = getNamePath(longestExistingPath)

        if (longestExistingNamePath == nodePath.path) {
            throw Exception("The provided path already exists")
        }

        val nodesToCreate: List<String> = nodePath.removePrefix(longestExistingNamePath).splitIgnoreEmpty(Constants.DELIMITER)

        setMetadataOfNodeToCreate(node)
        val listOfNodes = setMetaDataFromNode(node, nodesToCreate)

        /* path from new nodes to be created (will either be a suffix or an independent string)*/
        var suffixNodePath = ""
        for ((index, nodeToCreate) in listOfNodes.withIndex()) {
            when (index) {
                0 -> suffixNodePath = "${nodeToCreate.title}${Constants.DELIMITER}${nodeToCreate.id}"
                else -> suffixNodePath += "${Constants.DELIMITER}${nodeToCreate.title}${Constants.DELIMITER}${nodeToCreate.id}"
            }
        }

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
        return@runBlocking updatedNodeHierarchy.getDifferenceWithOldHierarchy(nodeHierarchyInformation)

    }

    private fun getUpdatedNodeHierarchyInformation(
        nodeHierarchyInformation: MutableList<String>,
        longestExistingPath: String,
        suffixNodePath: String
    ): List<String> {

        var nodePathToRemove: String? = null

        for (existingNodePath in nodeHierarchyInformation) {
            if (longestExistingPath == existingNodePath) {
                nodePathToRemove = existingNodePath
                break
            }
        }

        /* if nodePathToRemove != null , we are adding to a leaf path, so we remove that path*/
        when (nodePathToRemove != null) {
            true -> nodeHierarchyInformation.remove(nodePathToRemove)
        }

        nodeHierarchyInformation.add("$longestExistingPath${Constants.DELIMITER}$suffixNodePath")

        return nodeHierarchyInformation
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

    fun getNode(nodeID: String, workspaceID: String, bookmarkInfo: Boolean? = null, userID: String? = null): Entity? {
        val node =  (pageRepository.get(WorkspaceIdentifier(workspaceID), NodeIdentifier(nodeID), Node::class.java) )?.let { node -> orderBlocks(node) } as Node?
        if (bookmarkInfo == true && userID != null) {
            node?.isBookmarked = UserBookmarkService().isNodeBookmarkedForUser(nodeID, userID)
        }
        return node
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

        LOG.info(nodeIDList)
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

        LOG.info(elements)

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

        Page.populatePageWithCreatedFields(node, storedNode)

        node.dataOrder = createDataOrderForPage(node)

        /* to update block level details for accountability */
        val nodeChanged : Boolean = comparePageWithStoredPage(node, storedNode)

        if (!nodeChanged && (node.tags == storedNode.tags)) {
            return@runBlocking storedNode
        }

        /* to make the locking versions same */
        mergePageVersions(node, storedNode)

        launch { updateHierarchyIfRename(node, storedNode)}

        LOG.info("Updating node : $node")

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


    private fun updateHierarchyIfRename(node: Node, storedNode: Node){
        val newHierarchy = mutableListOf<String>()
        if(node.title != storedNode.title){
            val workspace = workspaceService.getWorkspace(node.workspaceIdentifier.id) as Workspace
            val currentHierarchy = workspace.nodeHierarchyInformation ?: listOf()
            for(nodePath in currentHierarchy){
                val idList = getIDPath(nodePath).getListOfNodes()
                val indexOfNodeID = idList.indexOf(node.id)
                if(indexOfNodeID != -1){
                    val nameList = getNamePath(nodePath).getListOfNodes() as MutableList
                    nameList[indexOfNodeID] = node.title
                    newHierarchy.add(nameList.mix(idList).convertToPathString())
                }
                else {
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
        nodeRequest.toNode(workspaceID, userID)


    fun getAllArchivedSnippetIDsOfWorkspace(workspaceID : String) : MutableList<String> {
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
                val archivedNodeName = archivedNodeIDToNameMap[nodeID] ?: throw Exception("Invalid nodeID : $nodeID")
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
                                if (indexOfParentNode != -1 && indexOfParentNode + 1 < activeNodesInPath.size
                                    && activeNodesInPath[indexOfParentNode + 1] == archivedNodeName
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


    fun deleteArchivedNodes(nodeIDRequest: WDRequest, workspaceID: String) : MutableList<String> = runBlocking {

        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)
        require(getAllArchivedSnippetIDsOfWorkspace(workspaceID).sorted() == nodeIDList.sorted()) { "The passed IDs should be present and archived" }
        val deletedNodesList : MutableList<String> = mutableListOf()
        for(nodeID in nodeIDList) {
            val tags = nodeRepository.getTags(nodeID, workspaceID)
            if(!tags.isNullOrEmpty()) launch { deleteTags(tags, nodeID, workspaceID) }
            repository.delete(WorkspaceIdentifier(workspaceID), NodeIdentifier(nodeID))?.also{
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

    fun getPublicNode(nodeID: String, workspaceID: String) : Node {
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
                "Source NodeID can't be equal to Destination NodeID"
            }

            val jobToGetSourceNodeWorkspaceID = async { checkIfNodeExistsForWorkspace(sourceNodeID, workspaceID) }
            val jobToGetDestinationNodeWorkspaceID = async { checkIfNodeExistsForWorkspace(destinationNodeID, workspaceID) }


            require(jobToGetSourceNodeWorkspaceID.await() && jobToGetDestinationNodeWorkspaceID.await()) {
                "NodeIDs don't exist"
            }
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
            nodeRepository.moveBlock(sourceNode.data?.get(0), workspaceID, sourceNodeID, destinationNodeID, it) }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }