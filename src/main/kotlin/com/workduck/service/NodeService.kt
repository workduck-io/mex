package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.workduck.models.*
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper


/**
 * contains all node related logic
 */
class NodeService {
    //Todo: Inject them from handlers

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when(System.getenv("TABLE_NAME")) {
            null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }


    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val nodeRepository : NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository, dynamoDBMapperConfig)


    fun createNode(jsonString : String) : Entity?{
        println("Should be created in the table : $tableName")
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id
        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        return repository.create(node)
    }


    fun getNode(nodeID: String): Entity? {
        return repository.get(NodeIdentifier(nodeID))
    }


    fun deleteNode(nodeID : String) : Identifier? {
        return repository.delete(NodeIdentifier(nodeID))
    }

    fun append(nodeID: String, jsonString: String) : Map<String, Any>? {

        val objectMapper = ObjectMapper().registerKotlinModule()
        val elements: MutableList<AdvancedElement> = objectMapper.readValue(jsonString)
        return nodeRepository.append(nodeID, elements)

    }

    fun updateNode(jsonString: String) : Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id
        node.createdAt = null

        /* In case workspace/ namespace have been updated, AK needs to be updated as well */
        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        return  repository.update(node)

    }


    fun getAllNodesWithWorkspaceID(workspaceID : String) : MutableList<String>? {

        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithNamespaceID(namespaceID : String, workspaceID: String) : MutableList<String>? {

        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)

    }

    fun updateNodeBlock(nodeID : String, blockData : String) : Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val element: AdvancedElement = objectMapper.readValue(blockData)
        val node : Node = getNode(nodeID) as Node

        for((index, block) in node.data!!.withIndex()){
            if(block.getID() == element.getID()){
                node.data!![index] = element
            }
        }

        node.updatedAt = System.currentTimeMillis()
        return  repository.update(node)

    }


}

fun main(){
    val jsonString : String = """
		{
			"id": "NODE1234",
            "namespaceIdentifier" : "NAMESPACE1",
            "workspaceIdentifier" : "WORKSPACE1",
			"data": [
			{
				"id": "sampleParentID",
                "elementType": "list",
                "childrenElements": [
                {
                    "id" : "sampleChildID",
                    "content" : "sample child content",
                    "elementType": "list",
                    "properties" :  { "bold" : true, "italic" : true  }
                }
                ]
			}
			]
		}
		"""

    val jsonString1 : String = """
		{
			"id": "NODE1234",
            "namespaceIdentifier" : "NAMESPACE2"
		}
		"""

    val jsonForAppend : String = """
        [
            {
            
            "id": "sampleParentID2",
            "content": "Sample Content 2",
            "elementType" : "list",
            "childrenElements": [
            {
               
                "id" : "sampleChildID2",
                "content" : "sample child content"
            }
            ]},
            {
            "id": "sampleParentID3",
            "content": "Sample Content 3",
            "elementType" : "random element type",
            "childrenElements": [
            {
                "id" : "sampleChildID3",
                "content" : "sample child content"
            }
            ]}
            
        ]
        """

    val jsonForEditBlock = """
        {
            "id" : "sampleParentID,
            "elementType": "list",
            "childrenElements": [
              {
                  "id" : "sampleChildID",
                  "content" : "edited child content",
                  "elementType": "list",
                  "properties" :  { "bold" : true, "italic" : true  }
              }
                ]
        }
      """

    //NodeService().createNode(jsonString)
    //println(NodeService().getNode("NODE1234"))
    //NodeService().updateNode(jsonString1)
    //NodeService().deleteNode("NODEF873GEFPVJQKV43NQMWQEJQGLF")
    //NodeService().jsonToObjectMapper(jsonString1)
    //NodeService().jsonToElement()
    //NodeService().append(jsonForAppend)
    //println(System.getenv("PRIMARY_TABLE"))
    //println(NodeService().getAllNodesWithNamespaceID("NAMESPACE1", "WORKSPACE1"))
    NodeService().updateNodeBlock("NODE1", jsonForEditBlock)
    //println(NodeService().getAllNodesWithWorkspaceID("WORKSPACE1"))
    //TODO("for list of nodes, I should be getting just namespace/workspace IDs and not the whole serialized object")

}

