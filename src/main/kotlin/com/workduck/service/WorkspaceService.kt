package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.WDRequest
import com.serverless.models.requests.WorkspaceRequest
import com.serverless.utils.Constants
import com.workduck.models.Entity
import com.workduck.models.HierarchyUpdateSource
import com.workduck.models.Identifier
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.Namespace
import com.workduck.models.Relationship
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.WorkspaceRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import com.workduck.utils.NodeHelper.getCommonPrefixNodePath
import com.workduck.utils.NodeHelper.getIDPath
import com.workduck.utils.NodeHelper.getNamePath
import com.workduck.utils.WorkspaceHelper
import com.workduck.utils.extensions.toWorkspace
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class WorkspaceService (

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    private val dynamoDB: DynamoDB = DynamoDB(client),
    private val mapper: DynamoDBMapper = DynamoDBMapper(client),

    val nodeService: NodeService= NodeService(),

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

    private val dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val workspaceRepository: WorkspaceRepository = WorkspaceRepository(dynamoDB, mapper, dynamoDBMapperConfig),
    private val repository: Repository<Workspace> =
        RepositoryImpl(dynamoDB, mapper, workspaceRepository, dynamoDBMapperConfig)

) {

    fun createWorkspace(workspaceRequest: WDRequest): Entity? {
        val workspaceID = Helper.generateNanoID(IdentifierType.WORKSPACE.name)
        val workspace: Workspace = createWorkspaceObjectFromWorkspaceRequest(workspaceRequest as WorkspaceRequest, workspaceID)
        return repository.create(workspace)
    }

    fun getWorkspace(workspaceID: String): Entity? {
        return repository.get(WorkspaceIdentifier(workspaceID), WorkspaceIdentifier(workspaceID), Workspace::class.java)
    }

    fun getNodeHierarchyOfWorkspace(workspaceID: String): Map<String, Any>  = runBlocking {
        val jobToGetWorkspace = async { getWorkspace(workspaceID) as Workspace }
        val jobToGetNamespaces = async { nodeService.namespaceService.getAllNamespaceData(workspaceID) }

        val hierarchyMap: MutableMap<String, Any> = mutableMapOf()

        val workspaceHierarchy : MutableMap<String, Any> = mutableMapOf()
        val namespaceHierarchy : MutableMap<String, Any> = mutableMapOf()

        constructWorkspaceInfo(jobToGetWorkspace.await(), workspaceHierarchy)
        constructNamespaceInfo(jobToGetNamespaces.await(), namespaceHierarchy)

        hierarchyMap[Constants.WORKSPACE_INFO] = workspaceHierarchy
        hierarchyMap[Constants.NAMESPACE_INFO] = namespaceHierarchy

        return@runBlocking hierarchyMap
    }

    fun getNodeHierarchyOfWorkspaceWithMetaData(workspaceID: String): Map<String, Any> = runBlocking {
        val jobToGetHierarchy =  async { getNodeHierarchyOfWorkspace(workspaceID) }
        val jobToGetNodesMetadata = async { nodeService.getMetadataForNodesOfWorkspace(workspaceID) }
        return@runBlocking mapOf("hierarchy" to jobToGetHierarchy.await(), "nodesMetadata" to jobToGetNodesMetadata.await())
    }

    fun getArchivedNodeHierarchyOfWorkspace(workspaceID: String): List<String> {
        return (getWorkspace(workspaceID) as Workspace).archivedNodeHierarchyInformation ?: listOf()
    }
    fun updateWorkspace(workspaceID: String, request: WDRequest) {
        val workspaceRequest: WorkspaceRequest = request as WorkspaceRequest
        workspaceRepository.updateWorkspaceName(workspaceID, workspaceRequest.name)
    }

    fun updateWorkspace(workspace: Workspace) {
        repository.update(workspace)
    }

    fun updateWorkspaceHierarchy(
        workspace: Workspace,
        newNodeHierarchy: List<String>,
        hierarchyUpdateSource: HierarchyUpdateSource
    ) {
        Workspace.populateHierarchiesAndUpdatedAt(workspace, newNodeHierarchy, workspace.archivedNodeHierarchyInformation)
        workspace.hierarchyUpdateSource = hierarchyUpdateSource
        updateWorkspace(workspace)
    }

    fun deleteWorkspace(workspaceID: String): Identifier? {
        return repository.delete(WorkspaceIdentifier(workspaceID), WorkspaceIdentifier(workspaceID),)
    }

    fun getWorkspaceData(workspaceIDList: List<String>): MutableMap<String, Workspace?> {
        return workspaceRepository.getWorkspaceData(workspaceIDList)
    }


    fun updateNodeHierarchyOnArchivingNode(workspace: Workspace, nodeID: String) {
        val updatedNodeHierarchy =
            getUpdatedNodeHierarchyOnDeletingNode(workspace.nodeHierarchyInformation ?: listOf(), nodeID)

        LOG.debug("Updated Node Hierarchy After Archiving node : $nodeID : $updatedNodeHierarchy")
        updateWorkspaceHierarchy(workspace, updatedNodeHierarchy, HierarchyUpdateSource.ARCHIVE)
    }

    // TODO(create a common interface for only Workspace and Namespace and combine these functions)
    private fun constructWorkspaceInfo(workspace: Workspace, workspaceHierarchyJson: MutableMap<String, Any>){
        val mapOfWorkspaceNameAndHierarchy = mutableMapOf<String, Any>()
        mapOfWorkspaceNameAndHierarchy[Constants.NAME] = workspace.name
        mapOfWorkspaceNameAndHierarchy[Constants.HIERARCHY] = workspace.nodeHierarchyInformation ?: listOf<String>()
        workspaceHierarchyJson.putIfAbsent(workspace.id, mapOfWorkspaceNameAndHierarchy)
    }

    private fun constructNamespaceInfo(namespaceList: List<Namespace>, namespaceHierarchyJson: MutableMap<String, Any>){
        for (namespace in namespaceList) {
            val mapOfNamespaceNameAndHierarchy = mutableMapOf<String, Any>()
            mapOfNamespaceNameAndHierarchy[Constants.NAME] = namespace.name
            mapOfNamespaceNameAndHierarchy[Constants.HIERARCHY] = namespace.nodeHierarchyInformation
            namespaceHierarchyJson.putIfAbsent(namespace.id, mapOfNamespaceNameAndHierarchy)
        }

    }

    private fun getUpdatedNodeHierarchyOnDeletingNode(
        nodeHierarchyInformation: List<String>,
        nodeID: String
    ): List<String> {
        val newNodeHierarchy = mutableListOf<String>()
        val updatedPaths = mutableListOf<String>()
        for (nodePath in nodeHierarchyInformation) {
            when (nodePath.contains(nodeID)) {
                false -> newNodeHierarchy.add(nodePath)
                true -> {
                    val nameList = getNamePath(nodePath).split(Constants.DELIMITER)
                    val idList = getIDPath(nodePath).split(Constants.DELIMITER)
                    val indexOfPassedNode = idList.indexOf(nodeID)

                    /* if index of passed nodeID is 2, that means we need to pick two nodes before that nodeID */
                    val newPath = nameList.take(indexOfPassedNode)
                        .zip(idList.take(indexOfPassedNode)) { name, id -> "$name${Constants.DELIMITER}$id" }
                        .joinToString(Constants.DELIMITER)
                    when (newPath.isNotEmpty()) {
                        true -> {
                            updatedPaths.add(newPath)
                        }
                    }
                }
            }
        }
        return WorkspaceHelper.removeRedundantPaths(updatedPaths.distinct(), newNodeHierarchy)
    }


    fun refreshNodeHierarchyForWorkspace(workspaceID: String) = runBlocking {

        val jobToGetListOfRelationships =
            async { RelationshipService().getHierarchyRelationshipsOfWorkspace(workspaceID, ItemStatus.ACTIVE) }
        val jobToGetMapOfNodeIDToName =
            async { NodeService().getAllNodeIDToNodeNameMap(workspaceID, ItemStatus.ACTIVE) }

        /* get single nodes with no hierarchy */
        val listOfRelationships = jobToGetListOfRelationships.await()
        val mapOfNodeIDToName = jobToGetMapOfNodeIDToName.await()
        LOG.debug("Node Map : $mapOfNodeIDToName")

        /* can be directly appended to node hierarchy */
        val nodeHierarchy = getNodesWithNoRelationship(listOfRelationships, mapOfNodeIDToName)

        val jobToGetNodePathsFromRelationships = async { getNodePaths(listOfRelationships, mapOfNodeIDToName) }
        val jobToGetWorkspace = async { getWorkspace(workspaceID) }

        nodeHierarchy += jobToGetNodePathsFromRelationships.await()
        val workspace = jobToGetWorkspace.await() as Workspace

        updateWorkspaceHierarchy(workspace, nodeHierarchy, HierarchyUpdateSource.REFRESH)
    }

    private fun getNodePaths(
        listOfRelationships: List<Relationship>,
        mapOfNodeIDToName: Map<String, String>
    ): MutableList<String> {

        val graph = constructGraphFromRelationships(listOfRelationships, mapOfNodeIDToName)

        return dfsForRelationshipsHelper(graph)
    }

    private fun dfsForRelationshipsHelper(graph: HashMap<String, MutableList<String>>): MutableList<String> {

        val visitedSet = mutableSetOf<String>()
        val nodeHierarchy = mutableListOf<String>()

        for ((startNode, _) in graph) {
            if (visitedSet.contains(startNode)) continue
            dfsForRelationships(
                graph,
                startNode,
                startNode,
                visitedSet,
                nodeHierarchy
            ) /* initial path is same as start node */
        }
        LOG.debug("nodeHierarchy From DFS : $nodeHierarchy")
        return nodeHierarchy
    }

    /* node is of the form nodeName#nodeID */
    private fun dfsForRelationships(
        graph: HashMap<String, MutableList<String>>,
        node: String,
        _nodePath: String,
        visitedSet: MutableSet<String>,
        nodeHierarchy: MutableList<String>
    ) {

        visitedSet.add(node)
        var nodePath = _nodePath

        for (childNode in graph[node]!!) {
            nodePath += "${Constants.DELIMITER}$childNode"
            if (graph.containsKey(childNode)) {
                if (!visitedSet.contains(childNode)) {
                    dfsForRelationships(graph, childNode, nodePath, visitedSet, nodeHierarchy)
                } else {
                    /* update existing paths in list which are actually suffix */
                    findSuffixInNodeHierarchyAndReplace(nodeHierarchy, childNode)
                }
            } else {
                nodeHierarchy.add(nodePath)
            }
            nodePath = nodePath.removeSuffix("${Constants.DELIMITER}$childNode")
        }
    }

    /*
    Case In Point -> Graph : A-B
                             B-C
                             C-D

    If instead of choosing A first, we start DFS from B, nodeHierarchy will have : BCD ( B and C have been marked visited )
    So now, when we come to A for DFS, B is already visited, => suffix of an actual path is present.
    Therefore, we find all nodePaths in nodeHierarchy which start with B, remove them, prefix them with A and insert back.
     */
    private fun findSuffixInNodeHierarchyAndReplace(nodeHierarchy: MutableList<String>, node: String) {
        val listOfSuffix = mutableListOf<String>()
        for (nodePath in nodeHierarchy) {
            if (getCommonPrefixNodePath(node, nodePath) == node) {
                listOfSuffix.add(nodePath)
            }
        }

        for (suffix in listOfSuffix) {
            nodeHierarchy.remove(suffix)
            nodeHierarchy.add("$node${Constants.DELIMITER}$suffix")
        }
    }

    private fun constructGraphFromRelationships(
        listOfRelationships: List<Relationship>,
        mapOfNodeIDToName: Map<String, String>
    ): HashMap<String, MutableList<String>> {
        val graph: HashMap<String, MutableList<String>> = hashMapOf()

        for (relationship in listOfRelationships) {
            val startNodeID: String = relationship.startNode.id
            val startNodeName: String = mapOfNodeIDToName[startNodeID] ?: throw Exception("Invalid Node ID")

            val endNodeID: String = relationship.endNode.id
            val endNodeName: String = mapOfNodeIDToName[endNodeID] ?: throw Exception("Invalid Node ID")

            if (!graph.containsKey("$startNodeName${Constants.DELIMITER}$startNodeID")) {
                graph["$startNodeName${Constants.DELIMITER}$startNodeID"] =
                    mutableListOf("$endNodeName${Constants.DELIMITER}$endNodeID")
            } else {
                graph["$startNodeName${Constants.DELIMITER}$startNodeID"]?.add("$endNodeName${Constants.DELIMITER}$endNodeID")
            }
        }

        return graph
    }

    private fun getNodesWithNoRelationship(
        listOfRelationships: List<Relationship>,
        mapOfNodeIDToName: Map<String, String>
    ): MutableList<String> {
        val listOfAloneNodes = mutableListOf<String>()

        for ((nodeID, nodeName) in mapOfNodeIDToName) {
            var aloneNode = true
            for (relationship in listOfRelationships) {
                if (nodeID == relationship.startNode.id || nodeID == relationship.endNode.id) {
                    aloneNode = false
                    break
                }
            }
            if (aloneNode) {
                listOfAloneNodes.add("$nodeName${Constants.DELIMITER}$nodeID")
            }
        }
        LOG.debug("List of Alone Nodes: $listOfAloneNodes")

        return listOfAloneNodes
    }

    private fun createWorkspaceObjectFromWorkspaceRequest(workspaceRequest: WorkspaceRequest, workspaceID: String): Workspace = workspaceRequest.toWorkspace(workspaceID)


    companion object {
        private val LOG = LogManager.getLogger(WorkspaceService::class.java)
    }
}