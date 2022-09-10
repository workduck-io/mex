package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.utils.Constants
import com.serverless.utils.getListOfNodes
import com.workduck.models.Element
import com.workduck.models.Workspace
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.ItemType
import com.workduck.models.Namespace
import com.workduck.models.Node
import com.workduck.models.Tag
import com.workduck.models.WorkspaceIdentifier
import com.workduck.service.NodeService
import com.workduck.utils.NodeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.apache.logging.log4j.LogManager
import kotlin.math.exp

class WorkspaceRepository(
    private val dynamoDB: DynamoDB,
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Workspace> {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }


    override fun get(pkIdentifier: Identifier, skIdentifier: Identifier, clazz: Class<Workspace>): Workspace? {
        TODO("Not yet implemented")
    }

    override fun create(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    override fun update(t: Workspace): Workspace {
        TODO("Not yet implemented")
    }

    override fun delete(pkIdentifier: Identifier, skIdentifier: Identifier): Identifier {
        TODO("Not yet implemented")
    }

    fun updateWorkspaceName(workspaceID: String, name: String){

        val table = dynamoDB.getTable(tableName)
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()
        expressionAttributeValues[":workspaceName"] = name


        val updateExpression = "set workspaceName = :workspaceName, updatedAt = :updatedAt"

        try {
            UpdateItemSpec().update(pk = workspaceID, sk = workspaceID, updateExpression = updateExpression,
                    conditionExpression = "attribute_exists(PK) and attribute_exists(SK)", expressionAttributeValues = expressionAttributeValues).let {
                table.updateItem(it)
            }

        }catch (e: ConditionalCheckFailedException){
            LOG.warn("Invalid WorkspaceID : $workspaceID")
        }


    }

    fun getWorkspaceData(workspaceIDList: List<String>): MutableMap<String, Workspace?> {
        val workspaceMap: MutableMap<String, Workspace?> = mutableMapOf()

        for (workspaceID in workspaceIDList) {
            val workspace: Workspace? = mapper.load(Workspace::class.java, workspaceID, workspaceID, dynamoDBMapperConfig)
            workspaceMap[workspaceID] = workspace
        }
        return workspaceMap

    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceRepository::class.java)
    }

    fun getMostPopularWorkspaces(){
        //val table = dynamoDB.getTable("test-mex")
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(ItemType.Workspace.name.uppercase())
        expressionAttributeValues[":SK"] = AttributeValue(ItemType.Workspace.name.uppercase())

        val x =  DynamoDBScanExpression().withFilterExpression("begins_with(PK, :PK) and begins_with(SK, :SK)")
                .withExpressionAttributeValues(expressionAttributeValues)

        val dbMapperConfig = DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement("test-mex"))
                .build()

        val listOfWorkspace = mapper.scan(Workspace::class.java, x, dbMapperConfig)

        val mapOfWorkspaceToActiveNodeIDs = mutableMapOf<String, Int>()
        for(workspace in listOfWorkspace){
            val activeHierarchy = workspace.nodeHierarchyInformation ?: listOf()
            val nodeIDs = activeHierarchy.map { nodePath ->
                NodeHelper.getIDPath(nodePath).getListOfNodes()
            }.flatten()

            val uniqueNodeIDs = nodeIDs.toSet()
            mapOfWorkspaceToActiveNodeIDs[workspace.id] = uniqueNodeIDs.size
        }

        println(mapOfWorkspaceToActiveNodeIDs)


        println("---------------------------------------------------------------------------------")

        val sortedMap = mapOfWorkspaceToActiveNodeIDs.toList().sortedBy { (_, value) -> value}.toMap()

        println(sortedMap)

    }

    fun editWorkspaceAndCreateNamespace(listOfWorkspaces: List<String>, nodeService: NodeService) {

        val dbMapperConfig = DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement("test-mex"))
                .build()
        for (workspaceID in listOfWorkspaces) {

            println("Doing work for ${workspaceID}........")
            val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
            expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
            expressionAttributeValues[":sk"] = AttributeValue().withS(workspaceID)

            val workspace = DynamoDBQueryExpression<Workspace>().query(keyConditionExpression = "PK = :pk and SK = :sk",
                    expressionAttributeValues = expressionAttributeValues).let {
                mapper.query(Workspace::class.java, it, dbMapperConfig).first()
            }

            val activeHierarchy = workspace.nodeHierarchyInformation
            val archivedHierarchy = workspace.archivedNodeHierarchyInformation
            val table = dynamoDB.getTable("staging-mex")
            val expressionAttributeValues1: MutableMap<String, Any> = HashMap()
            expressionAttributeValues1[":nodeHierarchyInformation"] = listOf<String>()
            expressionAttributeValues1[":archivedNodeHierarchyInformation"] = listOf<String>()


            val updateExpression = "set nodeHierarchyInformation = :nodeHierarchyInformation, archivedNodeHierarchyInformation = :archivedNodeHierarchyInformation"


            UpdateItemSpec().update(pk = workspaceID, sk = workspaceID, updateExpression = updateExpression,
                    conditionExpression = "attribute_exists(PK) and attribute_exists(SK)", expressionAttributeValues = expressionAttributeValues1).let {
                table.updateItem(it)
            }

            val namespace = Namespace(
                    name = "Personal",
                    workspaceIdentifier = WorkspaceIdentifier(workspace.id),
                    nodeHierarchyInformation = activeHierarchy!!,
                    archivedNodeHierarchyInformation = archivedHierarchy ?: listOf()

            )

            println("Creating namespace : ${namespace.id}.....")

            nodeService.namespaceService.createNamespace(namespace)

            println("Workspace to Namespace Mapping : $workspaceID ${namespace.id}")


        }
    }

    fun copyDataFromTop10Workspaces(listOfWorkspaces: List<String>, nodeService: NodeService){
        val dbMapperConfig = DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement("test-mex"))
                .build()

        for(workspaceID in listOfWorkspaces) {
            val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
            expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
            expressionAttributeValues[":sk"] = AttributeValue().withS(workspaceID)

             val workspace = DynamoDBQueryExpression<Workspace>().query(keyConditionExpression = "PK = :pk and SK = :sk",
                    expressionAttributeValues = expressionAttributeValues).let {
                mapper.query(Workspace::class.java, it, dbMapperConfig).first()
            }

            println("Doing work for ${workspace.id}........")
            println("Getting all nodes for ${workspace.id}........")


            val expressionAttributeValues2: MutableMap<String, AttributeValue> = HashMap()
            expressionAttributeValues2[":pk"] = AttributeValue().withS(workspaceID)
            expressionAttributeValues2[":sk"] = AttributeValue().withS(ItemType.Node.name.uppercase())

            val nodes = DynamoDBQueryExpression<Node>().query(keyConditionExpression = "PK = :pk and begins_with(SK, :sk)",
                    expressionAttributeValues = expressionAttributeValues2).let {
                mapper.query(Node::class.java, it, dbMapperConfig)
            }


            println("Total nodes in ${workspace.id} = ${nodes.size}")

            val dbMapperConfig1 = DynamoDBMapperConfig.Builder()
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement("staging-mex"))
                    .build()

            println("Creating ${workspace.id} in staging-mex")

            mapper.save(workspace, dbMapperConfig1)

            println("Creating nodes in  ${workspace.id} in staging-mex")

            nodeService.batchCreateNodes(nodes, dbMapperConfig1)

        }

    }

    fun updateAKForNodes(mapOfWorkspaceToNamespace : Map<String, String>){

        val dbMapperConfig = DynamoDBMapperConfig.Builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement("staging-mex"))
                .build()

        for((workspaceID, namespaceID) in mapOfWorkspaceToNamespace.entries){
            println("Updating nodes for $workspaceID, $namespaceID")
            val expressionAttributeValues2: MutableMap<String, AttributeValue> = HashMap()
            expressionAttributeValues2[":pk"] = AttributeValue().withS(workspaceID)
            expressionAttributeValues2[":sk"] = AttributeValue().withS(ItemType.Node.name.uppercase())

            val nodes = DynamoDBQueryExpression<Node>().query(keyConditionExpression = "PK = :pk and begins_with(SK, :sk)",
                    expressionAttributeValues = expressionAttributeValues2).let {
                mapper.query(Node::class.java, it, dbMapperConfig)
            }
            parallel(nodes, namespaceID)




        }

    }

    fun parallel(nodes: List<Node>, namespaceID: String) = runBlocking{
        var count = 0
        val jobToUpdate = CoroutineScope(Dispatchers.IO + Job()).async {
            supervisorScope {
                val deferredList = ArrayList<Deferred<*>>()
                for (node in nodes) {
                    count++
                    deferredList.add(
                            async {  updateNodeAK(node, namespaceID) }
                    )
                }
                deferredList.joinAll()
            }
        }
        jobToUpdate.await()
        println("$count nodes updated.")
    }

    fun updateNodeAK(node: Node, namespaceID: String){

        val table = dynamoDB.getTable("staging-mex")
        val expressionAttributeValues1: MutableMap<String, Any> = HashMap()
        expressionAttributeValues1[":namespaceID"] = namespaceID
        //expressionAttributeValues1[":archivedNodeHierarchyInformation"] = listOf<String>()


        val updateExpression = "set AK = :namespaceID"


        UpdateItemSpec().update(pk = node.workspaceIdentifier.id, sk = node.id, updateExpression = updateExpression,
                conditionExpression = "attribute_exists(PK) and attribute_exists(SK)", expressionAttributeValues = expressionAttributeValues1).let {
            table.updateItem(it)
        }


    }

}
