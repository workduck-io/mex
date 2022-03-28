package com.workduck.service

import abc
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.*
import com.workduck.models.AdvancedElement
import com.workduck.models.Entity
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.IdentifierType
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
import com.workduck.utils.NodeHelper
import com.workduck.utils.NodeHelper.getCommonPrefixNodePath
import com.workduck.utils.NodeHelper.getIDPath
import com.workduck.utils.NodeHelper.getNamePath
import com.workduck.utils.RelationshipHelper.findStartNodeOfEndNode
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import toNode

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

    val nodeRepository: NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig, client, tableName),
    val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository, dynamoDBMapperConfig)
) {

    fun createNode(node: Node, versionEnabled: Boolean): Entity? {
        LOG.info("Should be created in the table : $tableName")
        LOG.info("ENV TABLE : " + System.getenv("TABLE_NAME"))
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
            id = "${node.id}#VERSION",
            lastEditedBy = node.lastEditedBy,
            createdBy = node.createdBy,
            data = node.data,
            dataOrder = node.dataOrder,
            createdAt = node.createdAt,
            ak = node.ak,
            namespaceIdentifier = node.namespaceIdentifier,
            workspaceIdentifier = node.workspaceIdentifier,
            updatedAt = "UPDATED_AT#${node.updatedAt}"
        )
        nodeVersion.version = Helper.generateId("version")
        return nodeVersion
    }

    /* if operation is "create", will be used to create just a single leaf node */
    fun createAndUpdateNode(request: WDRequest?, workspaceID: String, versionEnabled: Boolean = false): Entity? =
        runBlocking {

            val nodeRequest: NodeRequest = request as NodeRequest
            val node: Node = createNodeObjectFromNodeRequest(nodeRequest, workspaceID) ?: return@runBlocking null

            val jobToGetStoredNode = async { getNode(node.id) as Node? }
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
                workspaceService.addNodePathToHierarchy(workspace.id, "$nodeTitle#$nodeID")
            }
            else -> {
                val nodeHierarchy = workspace.nodeHierarchyInformation as MutableList<String>? ?: mutableListOf()
                val nodePathsToAdd = mutableListOf<String>()
                val nodePathsToRemove = mutableListOf<String>()

                for (nodePath in nodeHierarchy) {
                    if (nodePath.endsWith(referenceID)) { /* referenceID was a leaf node */
                        nodePathsToAdd.add("$nodePath#$nodeTitle#$nodeID")
                        nodePathsToRemove.add(nodePath)
                    } else if (nodePath.contains(referenceID)) { /* referenceID is not leaf node, and we need to extract path till referenceID */
                        val endIndex = nodePath.indexOf(referenceID) + referenceID.length - 1
                        val pathTillRefID = nodePath.substring(0, endIndex + 1)
                        nodePathsToAdd.add("$pathTillRefID#$nodeTitle#$nodeID")
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
    fun refactor(wdRequest: WDRequest, workspaceID: String) = runBlocking {
        // This should be moved to handler ; considering this as a pre-filter
        val workspace = workspaceService.getWorkspace(workspaceID) as Workspace?
            ?: throw IllegalArgumentException("Invalid Workspace ID")

        val refactorNodePathRequest = wdRequest as RefactorRequest

        /* existingNodePath is path from root till last node in the path and not necessarily path till a leaf node */
        val existingNodePath = refactorNodePathRequest.existingNodePath
        val newNodePath = refactorNodePathRequest.newNodePath

        val lastNodeID = refactorNodePathRequest.nodeID
        val lastEditedBy = refactorNodePathRequest.lastEditedBy

        val existingNodes = existingNodePath.allNodes
        val newNodes = newNodePath.allNodes

        // Data model has ensures that list will never be empty
        when (existingNodePath.allNodes.last() != newNodePath.allNodes.last()) {
            true -> { /* need to rename last node from existing path to last node from new path */
                launch { renameNode(lastNodeID, newNodes.last(), lastEditedBy) }
            }
        }

        // a -> b -> c
        // a -> b -> x -> y -> t(c)
        // a-> e(c)
        // Handle this case
        val namesOfNodesToCreate = newNodes.minus(existingNodes.toSet()) as MutableList

        /* since the last node just needs renaming at max, we don't need to create it again */
        namesOfNodesToCreate.remove(newNodes.last())
        val nodesToCreate: List<Node> =
            setMetaDataForEmptyNodes(
                namesOfNodesToCreate,
                lastEditedBy,
                workspaceID,
                refactorNodePathRequest.namespaceID
            )
        launch { nodeRepository.createMultipleNodes(nodesToCreate) }
        launch {
            updateHierarchyInRefactor(
                existingNodePath.path,
                newNodePath.path,
                workspace,
                nodesToCreate,
                lastNodeID
            )
        }
    }

    private fun updateHierarchyInRefactor(
        existingNodeNamePath: NodePath,
        newNodePath: NodePath,
        workspace: Workspace,
        nodesToCreate: List<Node>,
        lastNodeID: String
    ) {

        val existingNodes = existingNodeNamePath.allNodes
        val newNodes = newNodePath.allNodes

        val nodeHierarchyInformation =
            workspace.nodeHierarchyInformation ?: throw NullPointerException("No Hierarchy Found")
        val newNodeHierarchy = mutableListOf<String>()

        for (nodePath in nodeHierarchyInformation) {
            val namePath = getNamePath(nodePath)
            /* if the current nodeNamePath has all the nodes from the passed path, we know we need to change this current path */
            if (getCommonPrefixNodePath(existingNodeNamePath.path, namePath).split("#") == existingNodes) {
                LOG.info("PASSED PATH : $existingNodeNamePath, CURRENT NAME PATH : $namePath")
                /* break new node path in 4 parts and combine later
                1. Unchanged Prefix Path
                2. Path due to new Nodes in between
                3. Path due to renaming of one node
                4. Unchanged Suffix Path
                 */

                val paths = mutableListOf<String>()

                val idPath = getIDPath(nodePath).split("#")
                LOG.info("ID PATH : $idPath")

                val namesOfUnchangedPrefixNodes =
                    existingNodes.dropLast(1) /* last node needs to be handled separately */
                val idsOfUnchangedPrefixNodes = idPath.subList(0, namesOfUnchangedPrefixNodes.size)
                val prefixString =
                    namesOfUnchangedPrefixNodes.zip(idsOfUnchangedPrefixNodes) { name, id -> "$name#$id" }
                        .joinToString("#")
                LOG.info("Prefix String : $prefixString")
                paths.add(prefixString)

                // path due to potential new nodes in between
                val newString = nodesToCreate.joinToString("#") { node -> "${node.title}#${node.id}" }
                LOG.info("newString : $newString")
                paths.add(newString)

                // rename string
                val renameNodePath = "${newNodes.last()}#$lastNodeID"  /* even if no need to rename, we're good */
                LOG.info("renameString : $renameNodePath")
                paths.add(renameNodePath)

                // suffix string
                val namesOfUnchangedSuffixNodes = namePath.removePrefix(existingNodeNamePath.path)
                    .splitIgnoreEmpty("#") /* get all the nodes after last node from passed existing path */
                val idsOfUnchangedSuffixNodes = idPath.takeLast(namesOfUnchangedSuffixNodes.size)
                val suffixString =
                    namesOfUnchangedSuffixNodes.zip(idsOfUnchangedSuffixNodes) { name, id -> "$name#$id" }
                        .joinToString("#")
                LOG.info("suffixString : $suffixString")
                paths.add(suffixString)

                newNodeHierarchy.add(paths.joinToString("#"))
                LOG.info("New path : " + newNodeHierarchy.last())
            } else {
                newNodeHierarchy.add(nodePath)
            }
        }

        workspaceService.updateWorkspaceHierarchy(workspace, newNodeHierarchy, HierarchyUpdateSource.NODE)

    }

    private fun renameNode(nodeID: String, newName: String, lastEditedBy: String) {
        nodeRepository.renameNode(nodeID, newName, lastEditedBy)
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
                    id = Helper.generateNanoID("${IdentifierType.NODE.name}"),
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


    fun bulkCreateNodes(request: WDRequest?, workspaceID: String) = runBlocking {
        val nodeRequest: NodeRequest? = request as NodeRequest?
        val node: Node =
            createNodeObjectFromNodeRequest(nodeRequest, workspaceID) ?: throw IllegalArgumentException("Invalid Body")

        val workspace: Workspace = workspaceService.getWorkspace(workspaceID) as Workspace

        val nodeHierarchyInformation = workspace.nodeHierarchyInformation as MutableList<String>

        val nodePath: String = nodeRequest?.nodePath ?: throw IllegalArgumentException("nodePath needs to be provided")

        val longestExistingPath = NodeHelper.getLongestExistingPath(nodeHierarchyInformation, nodePath)

        val longestExistingNamePath = getNamePath(longestExistingPath)

        if (longestExistingNamePath == nodePath) {
            throw Exception("The provided path already exists")
        }

        val nodesToCreate: List<String> = nodePath.removePrefix(longestExistingNamePath).splitIgnoreEmpty("#")

        setMetadataOfNodeToCreate(node)
        val listOfNodes = setMetaDataFromNode(node, nodesToCreate)

        /* path from new nodes to be created (will either be a suffix or an independent string)*/
        var suffixNodePath = ""
        for ((index, nodeToCreate) in listOfNodes.withIndex()) {
            when (index) {
                0 -> suffixNodePath = "${nodeToCreate.title}#${nodeToCreate.id}"
                else -> suffixNodePath += "#${nodeToCreate.title}#${nodeToCreate.id}"
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

        nodeHierarchyInformation.add("$longestExistingPath#$suffixNodePath")

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
            id = Helper.generateNanoID("${IdentifierType.NODE.name}_"),
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


    fun archiveNodes(nodeIDRequest: WDRequest, workspaceID: String): MutableList<String> = runBlocking {


        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest)

        val jobToGetWorkspace = async { workspaceService.getWorkspace(workspaceID) as Workspace }
        val jobToChangeNodeStatus =
            async { nodeRepository.unarchiveOrArchiveNodes(nodeIDList, workspaceID, ItemStatus.ARCHIVED) }

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
        nodeRepository.unarchiveOrArchiveNodes(nodeIDList, workspaceID, ItemStatus.ARCHIVED)
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
            e.createdAt = getCurrentTime()
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

        element.updatedAt = getCurrentTime()

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
                                currElement.updatedAt = getCurrentTime()
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

    private fun createNodeObjectFromNodeRequest(nodeRequest: NodeRequest?, workspaceID: String): Node? =
        nodeRequest.toNode(workspaceID)


    fun getMetaDataOfAllArchivedNodesOfWorkspace(workspaceID: String): MutableList<String>? {
        return nodeRepository.getAllArchivedNodesOfWorkspace(workspaceID)
    }

    fun unarchiveNodes(nodeIDRequest: WDRequest, workspaceID: String): List<String> = runBlocking {
        val nodeIDList = convertGenericRequestToList(nodeIDRequest as GenericListRequest) as MutableList
        val mapOfNodeIDToName = getArchivedNodesToRename(nodeIDList, workspaceID)

        for ((nodeID, _) in mapOfNodeIDToName) {
            nodeIDList.remove(nodeID)
        }

        val jobToUnarchiveAndRenameNodes = async { nodeRepository.unarchiveAndRenameNodes(mapOfNodeIDToName) }
        val jobToUnarchiveNodes =
            async { nodeRepository.unarchiveOrArchiveNodes(nodeIDList, workspaceID, ItemStatus.ACTIVE) }

        return@runBlocking jobToUnarchiveAndRenameNodes.await() + jobToUnarchiveNodes.await()
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
                                val activeNodesInPath = nodePath.split("#")
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

    /* this is called internally via trigger. We don't need to do sanity check for name here */
    fun unarchiveNodes(nodeIDList: List<String>, workspaceID: String): MutableList<String> {
        return nodeRepository.unarchiveOrArchiveNodes(nodeIDList, workspaceID, ItemStatus.ACTIVE)
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

    private fun workspaceLevelChecksForMovement(destinationNodeID: String, sourceNodeID: String, workspaceID: String) =
        runBlocking {
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

fun main() {
    val jsonForRefactor = """
         {
             "type" : "RefactorRequest",
             "existingNodePath": "A#B#D",
             "newNodePath": "A#B#F#X",
             "lastEditedBy": "Varun",
             "nodeID": "NODE_YKLY3zQQp4nNzrPqR9mVt"

         }
         """

    val nodeRequest = Helper.objectMapper.readValue<WDRequest>(jsonForRefactor)
    NodeService().refactor(nodeRequest, "WORKSPACE1")
}
