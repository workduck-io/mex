package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome
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
        println(node)
        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id
        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        node.dataOrder = createDataOrderForNode(node)

        return repository.create(node)
    }

    private fun createDataOrderForNode(node : Node) : MutableList<String> {

        val list = mutableListOf<String>()
        for(element in node.data!!){
            list  += element.getID()
        }
        return list
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

        val orderList = mutableListOf<String>()
        for(e in elements){
            orderList += e.getID()
        }

        return nodeRepository.append(nodeID, elements, orderList)

    }

    fun updateNode(jsonString: String) : Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id
        node.createdAt = null

        /* In case workspace/ namespace have been updated, AK needs to be updated as well */
        node.ak = "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"

        node.dataOrder = createDataOrderForNode(node)

        return  repository.update(node)

    }


    fun getAllNodesWithWorkspaceID(workspaceID : String) : MutableList<String>? {

        return nodeRepository.getAllNodesWithWorkspaceID(workspaceID)
    }

    fun getAllNodesWithNamespaceID(namespaceID : String, workspaceID: String) : MutableList<String>? {

        return nodeRepository.getAllNodesWithNamespaceID(namespaceID, workspaceID)

    }

    fun updateNodeBlock(nodeID : String, blockJson : String) : AdvancedElement? {

        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val element: AdvancedElement = objectMapper.readValue(blockJson)

        val blockData =  objectMapper.writeValueAsString(element)
        return nodeRepository.updateNodeBlock(nodeID, blockData, element.getID())

    }


}

fun main(){
    val jsonString : String = """
		{
			"id": "NODE1",
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
            
            "id": "xyz",
            "content": "Sample Content 4",
            "elementType" : "list",
            "childrenElements": [
            {
               
                "id" : "sampleChildID4",
                "content" : "sample child content"
            }
            ]},
            {
            "id": "abc",
            "content": "Sample Content 5",
            "elementType" : "random element type",
            "childrenElements": [
            {
                "id" : "sampleChildID5",
                "content" : "sample child content"
            }
            ]}
            
        ]
        """

    val jsonForEditBlock = """
        {
            "id" : "sampleParentID",
            "elementType": "list",
            "childrenElements": [
              {
                  "id" : "sampleChildID",
                  "content" : "edited child content - direct set - second try",
                  "elementType": "list",
                  "properties" :  { "bold" : true, "italic" : true  }
              }
                ]
        }
      """

    //NodeService().createNode(jsonString)
    //println(NodeService().getNode("NODE1"))
    //NodeService().updateNode(jsonString1)
    //NodeService().deleteNode("NODEF873GEFPVJQKV43NQMWQEJQGLF")
    //NodeService().jsonToObjectMapper(jsonString1)
    //NodeService().jsonToElement()
    NodeService().append("NODE1",jsonForAppend)
    //println(System.getenv("PRIMARY_TABLE"))
    //println(NodeService().getAllNodesWithNamespaceID("NAMESPACE1", "WORKSPACE1"))
    //NodeService().updateNodeBlock("NODE1", jsonForEditBlock)

   // NodeService().testOrderedMap()
    //println(NodeService().getAllNodesWithWorkspaceID("WORKSPACE1"))
    //TODO("for list of nodes, I should be getting just namespace/workspace IDs and not the whole serialized object")

}

