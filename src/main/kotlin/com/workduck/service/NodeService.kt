package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.BlockMovementRequest
import com.serverless.models.requests.ElementRequest
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.NodeRequest
import com.serverless.models.requests.RefactorNodePathRequest
import com.serverless.models.requests.WDRequest
import com.workduck.models.AdvancedElement
import com.workduck.models.Entity
import com.workduck.models.ItemStatus
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.Node
import com.workduck.models.NodeIdentifier
import com.workduck.models.NodeVersion
import com.workduck.models.Relationship
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.Helper.splitIgnoreEmpty
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

/**
 * contains all node related logic
 */
class NodeService {
    // Todo: Inject them from handlers

    private val objectMapper = Helper.objectMapper
    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig, client)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository, dynamoDBMapperConfig)

    fun createNode(node: Node, versionEnabled: Boolean): Entity? {
        LOG.info("Should be created in the table : $tableName")
        setMetadataOfNodeToCreate(node)
        LOG.info("Creating node : $node")

        return if (versionEnabled) {
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
            id = "${node.id}#VERSION", lastEditedBy = node.lastEditedBy, createdBy = node.createdBy,
            data = node.data, dataOrder = node.dataOrder, createdAt = node.createdAt, ak = node.ak, namespaceIdentifier = node.namespaceIdentifier,
            workspaceIdentifier = node.workspaceIdentifier, updatedAt = "UPDATED_AT#${node.updatedAt}"
        )
        nodeVersion.version = Helper.generateId("version")
        return nodeVersion
    }

    /* if operation is "create", will be used to create just a single leaf node */
    fun createAndUpdateNode(request: WDRequest?, workspaceID: String, versionEnabled: Boolean = false): Entity? = runBlocking {

        val nodeRequest: NodeRequest = request as NodeRequest
        val node: Node = createNodeObjectFromNodeRequest(nodeRequest, workspaceID) ?: return@runBlocking null
        val workspaceService = WorkspaceService()

        val jobToGetStoredNode = async { getNode(node.id) as Node? }
        val jobToGetWorkspace = async { node.workspaceIdentifier.id.let { (workspaceService.getWorkspace(it) as Workspace) } }

        return@runBlocking when (val storedNode = jobToGetStoredNode.await()) {
            null -> {
                val jobToCreateNode = async {createNode(node, versionEnabled)}
                val workspace = jobToGetWorkspace.await()
                launch {  updateNodeHierarchyInSingleCreate(nodeRequest.referenceID, node.id, node.title, workspace, workspaceService) }
                jobToCreateNode.await()
            }
            else -> {
                jobToGetWorkspace.cancel()
                updateNode(node, storedNode, versionEnabled)
            }
        }
    }

    private fun updateNodeHierarchyInSingleCreate(referenceID: String?, nodeID: String, nodeTitle: String, workspace: Workspace, workspaceService: WorkspaceService){
        when(referenceID){
            null -> {
                workspaceService.addNodePathToHierarchy(workspace.id, "$nodeTitle$$nodeID")
            }
            else -> {
                val nodeHierarchy = workspace.nodeHierarchyInformation as MutableList<String>? ?: mutableListOf()
                val nodePathsToAdd = mutableListOf<String>()
                val nodePathsToRemove = mutableListOf<String>()

                for(nodePath in nodeHierarchy){
                   if(nodePath.endsWith(referenceID)){ /* referenceID was a leaf node */
                       nodePathsToAdd.add("$nodePath#$nodeTitle$$nodeID")
                       nodePathsToRemove.add(nodePath)
                   } else if(nodePath.contains(referenceID)){ /* referenceID is not leaf node, and we need to extract path till referenceID */
                       val endIndex = nodePath.indexOf(referenceID) + referenceID.length - 1
                       val pathTillRefID = nodePath.substring(0, endIndex + 1)
                       nodePathsToAdd.add("$pathTillRefID#$nodeTitle$$nodeID")
                   } else {
                       continue
                   }
                }

                for(pathToRemove in nodePathsToRemove){
                    nodeHierarchy.remove(pathToRemove)
                }

                for(pathToAdd in nodePathsToAdd){
                    nodeHierarchy.add(pathToAdd)
                }

                workspace.nodeHierarchyInformation = nodeHierarchy
                workspace.updatedAt = System.currentTimeMillis()
                workspaceService.updateWorkspace(workspace)

            }
        }



    }

    /* these are name-paths */
    fun refactor(wdRequest: WDRequest, workspaceID: String) = runBlocking {

        val refactorNodePathRequest = wdRequest as RefactorNodePathRequest

        val existingNodePath = refactorNodePathRequest.existingNodePath
        val newNodePath = refactorNodePathRequest.newNodePath
        val lastNodeID = refactorNodePathRequest.nodeID
        val lastEditedBy = refactorNodePathRequest.lastEditedBy
        val namespaceID = refactorNodePathRequest.namespaceID

        require(existingNodePath != newNodePath) { "Old path and new path can't be same" }

        val existingNodes = existingNodePath.split("#")
        val newNodes = newNodePath.split("#")

        val nodeHierarchyInformation = (WorkspaceService().getWorkspace(workspaceID) as Workspace).nodeHierarchyInformation ?: listOf()

        when (existingNodes.last() == newNodes.last()) {
            false -> { /* need to rename last node from existing path to last node from new path */
               launch {  renameNode(lastNodeID, newNodes.last(), lastEditedBy) }
            }
        }

        val namesOfNodesToCreate = newNodes.minus(existingNodes.toSet()) as MutableList

        /* since the last node just needs renaming at max, we don't need to create it again */
        namesOfNodesToCreate.remove(newNodes.last())

        val nodesToCreate: List<Node> = setMetaDataForEmptyNodes(namesOfNodesToCreate, lastEditedBy, workspaceID, namespaceID)

        updateHierarchyInRefactor(existingNodePath, newNodePath, nodeHierarchyInformation, nodesToCreate)
    }

    private fun updateHierarchyInRefactor(existingNodePath: String, newNodePath: String, nodeHierarchyInformation: List<String>, nodesToCreate: List<Node>){

        val existingNodes = existingNodePath.split("#")
        val newNodes = newNodePath.split("#")

        val newNodeHierarchy = mutableListOf<String>()
        for(nodePath in nodeHierarchyInformation){
            val namePath = getNamePath(nodePath)
            if(existingNodePath.commonPrefixWith(namePath).splitIgnoreEmpty("#") == existingNodes){

                /* break new node path in 4 parts and combine later
                1. Unchanged Prefix Path
                2. Path due to new Nodes in between
                3. Path due to renaming of one node
                4. Unchanged Suffix Path
                 */

                val paths = mutableListOf<String>()

                val idPath = getIDPath(nodePath).split("#")

                val namesOfUnchangedPrefixNodes = existingNodes.dropLast(1) /* last node needs to be handled separately */
                val idsOfUnchangedPrefixNodes = idPath.subList(0, namesOfUnchangedPrefixNodes.size)
                val prefixString = namesOfUnchangedPrefixNodes.zip(idsOfUnchangedPrefixNodes) { name, id -> "$name#$id" }.joinToString("#")
                paths.add(prefixString)

                // path due to potential new nodes in between
                val newString = nodesToCreate.joinToString("#") { node -> "${node.title}#${node.id}" }
                paths.add(newString)

                // rename string
                val idOfLastNodeInExistingPath = idPath[existingNodes.size - 1]
                val renameString = "${newNodes.last()}#$idOfLastNodeInExistingPath"  /* even if no need to rename, we're good */
                paths.add(renameString)

                // suffix string
                val namesOfUnchangedSuffixNodes = namePath.removePrefix(existingNodePath).splitIgnoreEmpty("#")
                val idsOfUnchangedSuffixNodes = idPath.takeLast(namesOfUnchangedSuffixNodes.size)
                val suffixString = namesOfUnchangedSuffixNodes.zip(idsOfUnchangedSuffixNodes) { name, id -> "$name#$id" }.joinToString("#")
                paths.add(suffixString)

                newNodeHierarchy.add(paths.joinToString("#"))

            }
            else{
                newNodeHierarchy.add(nodePath)
            }
        }


    }

    private fun renameNode(nodeID: String, newName: String, lastEditedBy: String){
        nodeRepository.renameNode(nodeID, newName, lastEditedBy)
    }

    private fun setMetaDataForEmptyNodes(namesOfNodesToCreate: MutableList<String>, lastEditedBy: String, workspaceID: String, namespaceID: String?) : MutableList<Node> {
        val listOfNodes = mutableListOf<Node>()
        for (newNodeName in namesOfNodesToCreate) {
            listOfNodes.add(
                Node(
                    id = "NEED TO GET FUNCTION",
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

    /*
           A->B
           E->F
           [AB, EF]

           A->B->C->D , LO : AB
           [ABCD]

     */

    fun bulkCreateNodes(request: WDRequest?, workspaceID: String) {
        val nodeRequest: NodeRequest? = request as NodeRequest?
        val node: Node = createNodeObjectFromNodeRequest(nodeRequest, workspaceID) ?: throw IllegalArgumentException("Invalid Body")

        val workspace: Workspace = WorkspaceService().getWorkspace(workspaceID) as Workspace

        val nodeHierarchyInformation = workspace.nodeHierarchyInformation as MutableList<String>

        val nodePath: String? = nodeRequest?.nodePath

        val longestOverlappingPath = getLongestOverlappingPath(nodeHierarchyInformation, nodePath)

        if (longestOverlappingPath == nodePath) {
            throw Exception("The provided path already exists")
        }

        val nodesToCreate: List<String>? = nodePath?.let { it.removePrefix(it.commonPrefixWith(longestOverlappingPath)).splitIgnoreEmpty("#") }

        setMetadataOfNodeToCreate(node)
        val listOfNodes = setMetaDataFromNode(node, nodesToCreate)

        var suffixNodePath = ""

        for (nodeToCreate in listOfNodes) {
            suffixNodePath += "${nodeToCreate.title}#${nodeToCreate.id}"
        }

        workspace.nodeHierarchyInformation = getUpdatedNodeHierarchyInformation(nodeHierarchyInformation, longestOverlappingPath, suffixNodePath)
        workspace.updatedAt = System.currentTimeMillis()

        val listOfEntities = mutableListOf<Entity>()

        listOfEntities.add(workspace)
        listOfEntities += listOfNodes

        repository.createInBatch(listOfEntities)
    }

    private fun getUpdatedNodeHierarchyInformation(nodeHierarchyInformation: MutableList<String>, longestOverlappingPath: String, suffixNodePath: String): List<String> {

        var nodePathToRemove: String? = null

        for (existingNodePath in nodeHierarchyInformation) {
            if (longestOverlappingPath == getNamePath(existingNodePath)) {
                nodePathToRemove = existingNodePath
                break
            }
        }

        when (nodePathToRemove) {
            null -> nodeHierarchyInformation.add(suffixNodePath)
            else -> {
                nodeHierarchyInformation.remove(nodePathToRemove)
                nodeHierarchyInformation.add("$nodePathToRemove#$suffixNodePath")
            }
        }

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
            id = "NEED TO GET FUNCTION",
            title = newNodeName,
            workspaceIdentifier = node.workspaceIdentifier,
            namespaceIdentifier = node.namespaceIdentifier,
            createdBy = node.createdBy,
            lastEditedBy = node.lastEditedBy,
            createdAt = node.createdAt,
            ak = "${node.workspaceIdentifier.id}#${node.namespaceIdentifier?.id}",
            data = listOf()
        )

        newNode.updatedAt = node.updatedAt

        return newNode
    }

    /* sets AK, dataOrder, createdBy and accountability data of blocks of the node */
    private fun setMetadataOfNodeToCreate(node: Node) {

        node.ak = "${node.workspaceIdentifier.id}#${node.namespaceIdentifier?.id}"
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

    private fun getLongestOverlappingPath(nodeHierarchyInformation: List<String>?, nodePath: String?): String {

        var longestExistingPath = ""
        nodeHierarchyInformation?.let {
            for (existingNodePath in nodeHierarchyInformation) {
                val namePath = getNamePath(existingNodePath)
                val longestPath = nodePath?.commonPrefixWith(namePath) ?: ""
                if (longestPath.length > longestExistingPath.length) longestExistingPath = longestPath
            }
        }
        return longestExistingPath
    }

    /* nodePath is of the format : node1Name#node1ID#node2Name#node2ID.. */
    private fun getNamePath(nodePath: String): String {
        val nodeNames = mutableListOf<String>()
        nodePath.split("#").mapIndexed {
            index, string ->
            if (index % 2 == 0) nodeNames.add(string)
        }
        return nodeNames.joinToString("#")
    }

    private fun getIDPath(nodePath: String): String {
        val nodeNames = mutableListOf<String>()
        nodePath.split("#").mapIndexed {
            index, string ->
            if (index % 1 == 0) nodeNames.add(string)
        }
        return nodeNames.joinToString("#")
    }

    private fun createDataOrderForNode(node: Node): MutableList<String> {

        val list = mutableListOf<String>()
        for (element in node.data!!) {
            list += element.id
        }
        return list
    }

    fun getNode(nodeID: String, bookmarkInfo: Boolean? = null, userID: String? = null): Entity? {
        val node = repository.get(NodeIdentifier(nodeID)) as Node?
        if (bookmarkInfo == true && userID != null) {
            node?.isBookmarked = UserBookmarkService().isNodeBookmarkedForUser(nodeID, userID)
        }
        return node
    }

    /* basically archive the nodes */
    fun deleteNodes(nodeIDRequest: WDRequest, workspaceID: String): MutableList<String> = runBlocking {

        val workspaceService = WorkspaceService()
        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)

        val jobToGetWorkspace = async { (WorkspaceService().getWorkspace(workspaceID) as Workspace) }
        val jobToChangeNodeStatus = async { nodeRepository.unarchiveOrArchiveNodes(nodeIDList, ItemStatus.ARCHIVED) }

        val workspace = jobToGetWorkspace.await()

        // TODO(start using coroutines here if requests contain a lot of node ids)
        for (nodeID in nodeIDList) {
            workspaceService.updateNodeHierarchyOnDeletingNode(workspace, nodeID)
        }

        LOG.info(nodeIDList)
        return@runBlocking jobToChangeNodeStatus.await()
    }

    /* Getting called Internally via trigger. No need to update hierarchy */
    fun deleteNodes(nodeIDList: List<String>) = runBlocking {
        nodeRepository.unarchiveOrArchiveNodes(nodeIDList, ItemStatus.ARCHIVED)
    }

    fun convertGenericRequestToList(genericRequest: GenericListRequest): List<String> {
        return genericRequest.ids
    }

    fun getAllNodeIDToNodeNameMap(workspaceID: String, itemStatus: ItemStatus): Map<String, String> {
        return nodeRepository.getAllNodeIDToNodeNameMap(workspaceID, itemStatus)
    }

    fun append(nodeID: String, elementsListRequest: WDRequest): Map<String, Any>? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val elements = elementsListRequestConverted.elements

        LOG.info(elements)

        val orderList = mutableListOf<String>()
        var userID = ""
        for (e in elements) {
            orderList += e.id

            e.lastEditedBy = e.createdBy
            e.createdAt = System.currentTimeMillis()
            e.updatedAt = e.createdAt
            userID = e.createdBy as String
        }
        return nodeRepository.append(nodeID, userID, elements, orderList)
    }

    fun updateNode(node: Node, storedNode: Node, versionEnabled: Boolean): Entity? {

        /* set idCopy = id, createdAt = null, and set AK */
        Node.populateNodeWithSkAkAndCreatedAt(node, storedNode)

        node.dataOrder = createDataOrderForNode(node)

        /* to update block level details for accountability */
        val nodeChanged: Boolean = compareNodeWithStoredNode(node, storedNode)

        if (!nodeChanged) {
            return storedNode
        }

        /* to make the locking versions same */
        mergeNodeVersions(node, storedNode)

        LOG.info("Updating node : $node")
        // return nodeRepository.update(node)

        if (versionEnabled) {
            /* if the time diff b/w the latest version ( in version table ) and current node's updatedAt is < 5 minutes, don't create another version */
            if (node.updatedAt - storedNode.lastVersionCreatedAt!! < 300000) {
                node.lastVersionCreatedAt = storedNode.lastVersionCreatedAt
                return repository.update(node)
            }
            node.lastVersionCreatedAt = node.updatedAt
            checkNodeVersionCount(node, storedNode.nodeVersionCount)

            val nodeVersion = createNodeVersionFromNode(node)
            nodeVersion.createdAt = storedNode.createdAt
            nodeVersion.createdBy = storedNode.createdBy

            return nodeRepository.updateNodeWithVersion(node, nodeVersion)
        } else {
            return repository.update(node)
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

    fun updateNodeBlock(nodeID: String, elementsListRequest: WDRequest): AdvancedElement? {

        val elementsListRequestConverted = elementsListRequest as ElementRequest
        val element = elementsListRequestConverted.elements.let { it[0] }

        element.updatedAt = System.currentTimeMillis()

        // TODO(since we directly set the block info, createdAt and createdBy get lost since we're not getting anything from ddb)
        val blockData = objectMapper.writeValueAsString(element)

        return nodeRepository.updateNodeBlock(nodeID, blockData, element.id, element.lastEditedBy as String)
    }

    private fun mergeNodeVersions(node: Node, storedNode: Node) {

        /* if the same user edited the node the last time, he can overwrite anything */
        if (node.lastEditedBy == storedNode.lastEditedBy) {
            node.version = storedNode.version
            return
        }
        /* currently just handling when more blocks have been added */

        /* not handling the case when
            1. same block(s) has/have been edited
            2. some blocks deleted either by user1 or user2
        */
        val storedNodeDataOrder = storedNode.dataOrder
        val sentDataOrder = node.dataOrder
        val finalDataOrder = mutableListOf<String>()

        // very basic handling of maintaining rough order amongst blocks
        if (storedNodeDataOrder != null && sentDataOrder != null) {

            for (storedNodeID in storedNodeDataOrder) {
                finalDataOrder.add(storedNodeID)
            }

            for (storedNodeID in storedNodeDataOrder) {
                for (sentNodeID in sentDataOrder) {
                    if (storedNodeID == sentNodeID && storedNodeID !in finalDataOrder) {
                        finalDataOrder.add(storedNodeID)
                    }
                }
            }

            for (sentNodeID in sentDataOrder) {
                if (sentNodeID !in finalDataOrder) finalDataOrder.add(sentNodeID)
            }
        }

        node.dataOrder = finalDataOrder
        node.version = storedNode.version

        // TODO(explore autoMerge cmd line)
    }

    private fun compareNodeWithStoredNode(node: Node, storedNode: Node): Boolean {
        var nodeChanged = false

        /* in case a block has been deleted */
        if (node.data != storedNode.data) nodeChanged = true

        if (node.data != null) {
            for (currElement in node.data!!) {
                var isPresent = false
                if (storedNode.data != null) {
                    for (storedElement in storedNode.data!!) {
                        if (storedElement.id == currElement.id) {
                            isPresent = true

                            /* if the block has not been updated */
                            if (currElement == storedElement) {
                                currElement.createdAt = storedElement.createdAt
                                currElement.updatedAt = storedElement.updatedAt
                                currElement.createdBy = storedElement.createdBy
                                currElement.lastEditedBy = storedElement.lastEditedBy
                            }

                            /* when the block has been updated */
                            else {
                                nodeChanged = true
                                currElement.createdAt = storedElement.createdAt
                                currElement.updatedAt = System.currentTimeMillis()
                                currElement.createdBy = storedElement.createdBy
                                currElement.lastEditedBy = node.lastEditedBy
                            }
                        }
                    }

                    if (!isPresent) {
                        nodeChanged = true
                        currElement.createdAt = node.updatedAt
                        currElement.updatedAt = node.updatedAt
                        currElement.createdBy = node.lastEditedBy
                        currElement.lastEditedBy = node.lastEditedBy
                    }
                }
            }
        }
        return nodeChanged
    }

    private fun createNodeObjectFromNodeRequest(nodeRequest: NodeRequest?, workspaceID: String): Node? {
        val node = nodeRequest?.let {
            Node(
                id = nodeRequest.id,
                title = nodeRequest.title,
                namespaceIdentifier = nodeRequest.namespaceIdentifier,
                workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                lastEditedBy = nodeRequest.lastEditedBy,
                tags = nodeRequest.tags,
                data = nodeRequest.data
            )
        }

        return node
    }

    fun getMetaDataOfAllArchivedNodesOfWorkspace(workspaceID: String): MutableList<String>? {
        return nodeRepository.getAllArchivedNodesOfWorkspace(workspaceID)
    }

    fun unarchiveNodes(nodeIDRequest: WDRequest, workspaceID: String): List<String> = runBlocking {
        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest) as MutableList
        val mapOfNodeIDToName = getNodeNamesToChange(nodeIDList, workspaceID)

        for ((nodeID, _) in mapOfNodeIDToName) {
            nodeIDList.remove(nodeID)
        }

        val jobToUnarchiveAndRenameNodes = async { nodeRepository.unarchiveAndRenameNodes(mapOfNodeIDToName) }
        val jobToUnarchiveNodes = async { nodeRepository.unarchiveOrArchiveNodes(nodeIDList, ItemStatus.ACTIVE) }

        return@runBlocking jobToUnarchiveAndRenameNodes.await() + jobToUnarchiveNodes.await()
    }

    private fun getNodeNamesToChange(nodeIDList: List<String>, workspaceID: String): Map<String, String> = runBlocking {
        val jobToGetWorkspace = async { WorkspaceService().getWorkspace(workspaceID) as Workspace }
        val jobToGetArchivedNodeIDToNameMap = async { getAllNodeIDToNodeNameMap(workspaceID, ItemStatus.ARCHIVED) }
        val jobToGetArchivedHierarchyRelationship = async { RelationshipService().getHierarchyRelationshipsOfWorkspace(workspaceID, ItemStatus.ARCHIVED) }

        val nodeHierarchyInformation = jobToGetWorkspace.await().nodeHierarchyInformation ?: listOf()
        val archivedNodeIDToNameMap = jobToGetArchivedNodeIDToNameMap.await()
        val archivedHierarchyRelationships = jobToGetArchivedHierarchyRelationship.await()

        val mapOfNodeIDToNodeName = mutableMapOf<String, String>()
        for (nodeID in nodeIDList) {
            val archivedNodeName = archivedNodeIDToNameMap[nodeID] ?: continue
            for (nodePath in nodeHierarchyInformation) {
                if (nodePath.contains(archivedNodeName)) {
                    val archivedNodeParentID = findStartNodeOfEndNode(archivedHierarchyRelationships, nodeID)

                    when (archivedNodeParentID == null) {
                        true -> { /* node to be un-archived is a root node */
                            if (nodePath.startsWith(archivedNodeName)) {
                                mapOfNodeIDToNodeName[nodeID] = archivedNodeName
                            }
                        }
                        false -> {
                            val activeNodesInPath = nodePath.split("#")
                            val indexOfParentNode = activeNodesInPath.indexOf(archivedNodeParentID)
                            if (indexOfParentNode + 1 < activeNodesInPath.size && activeNodesInPath[indexOfParentNode + 1] == archivedNodeName) {
                                mapOfNodeIDToNodeName[nodeID] = archivedNodeName
                            }
                        }
                    }
                }
            }
        }
        return@runBlocking mapOfNodeIDToNodeName
    }

    private fun findStartNodeOfEndNode(relationshipList: List<Relationship>, endNodeID: String): String? {
        for (relationship in relationshipList) {
            if (relationship.endNode.id == endNodeID) {
                return relationship.startNode.id
            }
        }
        return null
    }

    fun unarchiveNodes(nodeIDList: List<String>): MutableList<String> {
        return nodeRepository.unarchiveOrArchiveNodes(nodeIDList, ItemStatus.ACTIVE)
    }

    fun deleteArchivedNodes(nodeIDRequest: WDRequest): MutableList<String> {

        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)
        val deletedNodesList: MutableList<String> = mutableListOf()
        for (nodeID in nodeIDList) {
            repository.delete(NodeIdentifier(nodeID))?.also {
                deletedNodesList.add(it.id)
            }
        }
        return deletedNodesList
    }

//    fun updateNodePath(wdRequest: WDRequest) {
//
//        val nodePathRefactorRequest = (wdRequest as NodePathRefactorRequest)
//
//        val newRelationship = Relationship(
//            sourceNode = NodeIdentifier(nodePathRefactorRequest.newParentID),
//            startNode = NodeIdentifier(nodePathRefactorRequest.newParentID),
//            endNode = NodeIdentifier(nodePathRefactorRequest.nodeID),
//            type = RelationshipType.LINKED
//        )
//
//        val oldRelationship = Relationship(
//            sourceNode = NodeIdentifier(nodePathRefactorRequest.currentParentID),
//            startNode = NodeIdentifier(nodePathRefactorRequest.currentParentID),
//            endNode = NodeIdentifier(nodePathRefactorRequest.nodeID),
//            type = RelationshipType.LINKED
//        )
//
//        nodeRepository.updateLinkedRelationship(oldRelationship, newRelationship)
//    }

    companion object {
        private val LOG = LogManager.getLogger(NodeService::class.java)
    }

    fun makeNodePublic(nodeID: String) {
        nodeRepository.toggleNodePublicAccess(nodeID, 1)
    }

    fun makeNodePrivate(nodeID: String) {
        nodeRepository.toggleNodePublicAccess(nodeID, 0)
    }

    fun getPublicNode(nodeID: String): Node? {
        return nodeRepository.getPublicNode(nodeID)
    }

    fun copyOrMoveBlock(wdRequest: WDRequest, workspaceID: String) {

        val copyOrMoveBlockRequest = wdRequest as BlockMovementRequest
        val destinationNodeID = copyOrMoveBlockRequest.destinationNodeID
        val sourceNodeID = copyOrMoveBlockRequest.sourceNodeID
        val blockID = copyOrMoveBlockRequest.blockID

        workspaceLevelChecksForMovement(destinationNodeID, sourceNodeID, workspaceID)

        when (copyOrMoveBlockRequest.action.lowercase()) {
            "copy" -> copyBlock(blockID, sourceNodeID, destinationNodeID)
            "move" -> moveBlock(blockID, sourceNodeID, destinationNodeID)
            else -> throw IllegalArgumentException("Invalid action")
        }
    }
    private fun workspaceLevelChecksForMovement(destinationNodeID: String, sourceNodeID: String, workspaceID: String) = runBlocking {
        require(destinationNodeID != sourceNodeID) {
            "Source NodeID can't be equal to Destination NodeID"
        }

        val jobToGetSourceNodeWorkspaceID = async { getWorkspaceIDOfNode(sourceNodeID) }
        val jobToGetDestinationNodeWorkspaceID = async { getWorkspaceIDOfNode(destinationNodeID) }

        val sourceNodeWorkspaceID = jobToGetSourceNodeWorkspaceID.await()

        require(sourceNodeWorkspaceID == jobToGetDestinationNodeWorkspaceID.await()) {
            "NodeIDs should belong to same workspace"
        }

        require(sourceNodeWorkspaceID == workspaceID) {
            "Passed NodeIDs should belong to the current workspace"
        }
    }

    private fun getWorkspaceIDOfNode(nodeID: String): String {
        return nodeRepository.getWorkspaceIDOfNode(nodeID)
    }

    private fun copyBlock(blockID: String, sourceNodeID: String, destinationNodeID: String) {
        /* this node contains only valid block info and dataOrder info */
        val sourceNode: Node? = nodeRepository.getBlock(sourceNodeID, blockID)

        val block = sourceNode?.data?.get(0)
        val userID = block?.createdBy as String

        nodeRepository.append(destinationNodeID, userID, listOf(block), mutableListOf(block.id))
    }

    private fun moveBlock(blockID: String, sourceNodeID: String, destinationNodeID: String) {
        /* this node contains only valid block info and dataOrder info  */
        val sourceNode: Node? = nodeRepository.getBlock(sourceNodeID, blockID)

        // TODO(list remove changes order of the original elements )
        sourceNode?.dataOrder?.let {
            it.remove(blockID)
            nodeRepository.moveBlock(sourceNode.data?.get(0), sourceNodeID, destinationNodeID, it)
        }
    }
}

fun main() = runBlocking{
    val jsonString: String = """
		{
            "type" : "NodeRequest",
            "lastEditedBy" : "USERVarun",
			"id": "NODE1",
            "namespaceIdentifier" : "NAMESPACE1",
            "workspaceIdentifier" : "WORKSPACE1",
			"data": [
			{
				"id": "sampleParentID",
                "elementType": "paragraph",
                "children": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content 1",
                    "elementType": "paragraph",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			},
            {
				"id": "1234",
                "elementType": "paragraph",
                "children": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content",
                    "elementType": "paragraph",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			}
			]
		}
		"""

    val jsonString1: String = """
        
    {
        "type" : "NodeRequest",
        "lastEditedBy" : "Varun",
        "id": "NODE1",
        "namespaceIdentifier" : "NAMESPACE1",
        "workspaceIdentifier" : "WORKSPACE1",
        "data": [
        {
            "id": "sampleParentID",
            "elementType": "paragraph",
            "children": [
            {
                "id" : "sampleChildID",
                "content" : "sample child content 1",
                "elementType": "paragraph",
                "properties" :  { "bold" : true, "italic" : true  }
            }
            ]
        }]
        
    }
    """

    val jsonForAppend: String = """
        [
            {
            "createdBy" : "Varun",
            "id": "xyz",
            "content": "Sample Content 4",
            "elementType" : "list",
            "children": [
            {
               
                "id" : "sampleChildID4",
                "content" : "sample child content"
            }
            ]},
            {
            "createdBy" : "Varun",
            "id": "abc",
            "content": "Sample Content 5",
            "elementType" : "random element type",
            "children": [
            {
                "id" : "sampleChildID5",
                "content" : "sample child content"
            }
            ]}
            
        ]
        """

    val jsonForEditBlock = """
        {
            "lastEditedBy" : "Varun",
            "id" : "sampleParentID",
            "elementType": "list",
            "children": [
              {
                  "id" : "sampleChildID",
                  "content" : "edited child content - direct set - second tryy",
                  "elementType": "list",
                  "properties" :  { "bold" : true, "italic" : true  }
              }
                ]
        }
      """



}
