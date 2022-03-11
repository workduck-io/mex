package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.WDRequest
import com.serverless.models.requests.WorkspaceRequest
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.ItemStatus
import com.workduck.models.Relationship
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.WorkspaceRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.NodeHelper.getCommonPrefixNodePath
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class WorkspaceService {

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

    private val workspaceRepository: WorkspaceRepository = WorkspaceRepository(dynamoDB, mapper, dynamoDBMapperConfig)
    private val repository: Repository<Workspace> = RepositoryImpl(dynamoDB, mapper, workspaceRepository, dynamoDBMapperConfig)

    fun createWorkspace(workspaceRequest: WDRequest?): Entity? {
        val workspace : Workspace = createWorkspaceObjectFromWorkspaceRequest(workspaceRequest as WorkspaceRequest?) ?: return null
        LOG.info("Creating workspace : $workspace")
        return repository.create(workspace)
    }

    fun getWorkspace(workspaceID: String): Entity? {
        LOG.info("Getting workspace with id : $workspaceID")
        return repository.get(WorkspaceIdentifier(workspaceID))
    }

    fun getNodeHierarchyOfWorkspace(workspaceID: String): List<String>? {
        return (getWorkspace(workspaceID) as Workspace).nodeHierarchyInformation
    }

    fun updateWorkspace(workspaceRequest: WDRequest?): Entity? {
        val workspace: Workspace = createWorkspaceObjectFromWorkspaceRequest(workspaceRequest as WorkspaceRequest?) ?: return null

        workspace.createdAt = null

        LOG.info("Updating workspace : $workspace")
        return repository.update(workspace)
    }

    fun updateWorkspace(workspace: Workspace) {
        LOG.info("Updating workspace : $workspace")
        repository.update(workspace)
    }

    fun deleteWorkspace(workspaceID: String): Identifier? {
        LOG.info("Deleting workspace with id : $workspaceID")
        return repository.delete(WorkspaceIdentifier(workspaceID))
    }

    fun getWorkspaceData(workspaceIDList: List<String>): MutableMap<String, Workspace?>? {
        LOG.info("Getting workspaces with ids : $workspaceIDList")
        return workspaceRepository.getWorkspaceData(workspaceIDList)
    }

    fun addNodePathToHierarchy(workspaceID: String, nodePath: String){
        workspaceRepository.addNodePathToHierarchy(workspaceID, nodePath)
    }


    fun updateNodeHierarchyOnDeletingNode(workspace: Workspace, nodeID: String) {
        val updatedNodeHierarchy = getUpdatedNodeHierarchyOnDeletingNode(workspace.nodeHierarchyInformation ?: listOf(), nodeID)
        workspace.nodeHierarchyInformation = updatedNodeHierarchy
        workspace.updatedAt = System.currentTimeMillis()
        repository.update(workspace)

    }

    private fun getUpdatedNodeHierarchyOnDeletingNode(nodeHierarchyInformation: List<String>, nodeID: String): MutableList<String>{
        val updatedNodeHierarchy = mutableListOf<String>()
        for(nodePath in nodeHierarchyInformation){
            when(nodePath.contains(nodeID)){
                false -> updatedNodeHierarchy.add(nodePath)
                true -> {
                    val nodeNameAndIDList = nodePath.split("#") as MutableList
                    val indexOfName = nodeNameAndIDList.indexOf(nodeID) - 1
                    nodeNameAndIDList.remove(nodeNameAndIDList[indexOfName])
                    nodeNameAndIDList.remove(nodeID)
                    updatedNodeHierarchy.add(nodeNameAndIDList.joinToString("#"))
                }
            }
        }
        return updatedNodeHierarchy
    }


    fun refreshNodeHierarchyForWorkspace(workspaceID: String) = runBlocking{

        val jobToGetListOfRelationships = async { RelationshipService().getHierarchyRelationshipsOfWorkspace(workspaceID, ItemStatus.ACTIVE) }
        val jobToGetMapOfNodeIDToName = async { NodeService().getAllNodeIDToNodeNameMap(workspaceID, ItemStatus.ACTIVE) }

        /* get single nodes with no hierarchy */
        val listOfRelationships = jobToGetListOfRelationships.await()
        val mapOfNodeIDToName = jobToGetMapOfNodeIDToName.await()
        LOG.info("Node Map : $mapOfNodeIDToName")

        /* can be directly appended to node hierarchy */
        val nodeHierarchy = getNodesWithNoRelationship(listOfRelationships, mapOfNodeIDToName)

        val jobToGetNodePathsFromRelationships = async {  getNodePaths(listOfRelationships, mapOfNodeIDToName) }
        val jobToGetWorkspace = async { getWorkspace(workspaceID) }

        nodeHierarchy += jobToGetNodePathsFromRelationships.await()
        val workspace = jobToGetWorkspace.await() as Workspace

        workspace.nodeHierarchyInformation = nodeHierarchy
        workspace.updatedAt = System.currentTimeMillis()
        LOG.info(nodeHierarchy)
        repository.update(workspace)
    }



    private fun getNodePaths(listOfRelationships : List<Relationship>, mapOfNodeIDToName : Map<String, String>) : MutableList<String>{

        val graph = constructGraphFromRelationships(listOfRelationships, mapOfNodeIDToName)

        return dfsForRelationshipsHelper(graph)

    }

    private fun dfsForRelationshipsHelper(graph: HashMap<String, MutableList<String>>) : MutableList<String>{

        val visitedSet = mutableSetOf<String>()
        val nodeHierarchy = mutableListOf<String>()

        LOG.info("Graph : $graph")
        for((startNode, _) in graph){
            if(visitedSet.contains(startNode)) continue
            dfsForRelationships(graph, startNode, startNode, visitedSet, nodeHierarchy) /* initial path is same as start node */

        }
        LOG.info("nodeHierarchy From DFS : $nodeHierarchy")
        return nodeHierarchy
    }

    /* node is of the form nodeName#nodeID */
    private fun dfsForRelationships(graph: HashMap<String, MutableList<String>>, node : String, _nodePath : String, visitedSet : MutableSet<String>, nodeHierarchy: MutableList<String>){

        visitedSet.add(node)
        var nodePath = _nodePath

        for(childNode in graph[node]!!){
            nodePath += "#$childNode"
            if(graph.containsKey(childNode)){
                if(!visitedSet.contains(childNode)) {
                    dfsForRelationships(graph, childNode, nodePath, visitedSet, nodeHierarchy)
                }
                else{
                    /* update existing paths in list which are actually suffix */
                    findSuffixInNodeHierarchyAndReplace(nodeHierarchy, childNode)
                }
            } else {
                nodeHierarchy.add(nodePath)
            }
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
    private fun findSuffixInNodeHierarchyAndReplace(nodeHierarchy: MutableList<String>, node: String){
        val listOfSuffix = mutableListOf<String>()
        for(nodePath in nodeHierarchy){
            if(getCommonPrefixNodePath(node, nodePath) == node){
                listOfSuffix.add(nodePath)
            }
        }

        for(suffix in listOfSuffix){
            nodeHierarchy.remove(suffix)
            nodeHierarchy.add("$node#$suffix")
        }
    }


    private fun constructGraphFromRelationships(listOfRelationships : List<Relationship>, mapOfNodeIDToName : Map<String, String>) : HashMap<String, MutableList<String>>{
        val graph: HashMap<String, MutableList<String>> = hashMapOf()

        for(relationship in listOfRelationships){
            val startNodeID : String = relationship.startNode.id
            val startNodeName: String = mapOfNodeIDToName[startNodeID] ?: throw Exception("Invalid Node ID")

            val endNodeID : String = relationship.endNode.id
            val endNodeName: String = mapOfNodeIDToName[endNodeID] ?: throw Exception("Invalid Node ID")

            if(!graph.containsKey("$startNodeName#$startNodeID")){
                graph["$startNodeName#$startNodeID"] = mutableListOf("$endNodeName#$endNodeID")
            } else {
                graph["$startNodeName#$startNodeID"]?.add("$endNodeName#$endNodeID")
            }
        }

        return graph
    }

    private fun getNodesWithNoRelationship(listOfRelationships : List<Relationship>, mapOfNodeIDToName : Map<String, String>) : MutableList<String>{
        val listOfAloneNodes  = mutableListOf<String>()

        for((nodeID, nodeName) in mapOfNodeIDToName){
            var aloneNode = true
            for(relationship in listOfRelationships){
                if(nodeID == relationship.startNode.id || nodeID == relationship.endNode.id){
                    aloneNode = false
                    break
                }
            }
            if(aloneNode){
                listOfAloneNodes.add("$nodeName#$nodeID")
            }
        }
        LOG.info("List of Alone Nodes: $listOfAloneNodes")

        return listOfAloneNodes
    }


    private fun createWorkspaceObjectFromWorkspaceRequest(workspaceRequest : WorkspaceRequest?) : Workspace?{
        return workspaceRequest?.let {
            Workspace(id = workspaceRequest.id,
                    name = workspaceRequest.name)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceService::class.java)
    }
}

fun main() {
    val json: String = """
		{
            "type": "WorkspaceRequest",
			"id": "WORKSPACE1",
			"name": "WorkDuck"
		}
		"""

    val jsonUpdate: String = """
		{
            "type": "WorkspaceRequest",
			"id" : "WORKSPACE1",
			"name" : "WorkDuck Pvt. Ltd. Blrrrrrr"
		}
		"""
}
