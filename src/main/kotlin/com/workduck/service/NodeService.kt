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

    private val tableName: String = System.getenv("TABLE_NAME")

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val nodeRepository : NodeRepository = NodeRepository(mapper, dynamoDB, dynamoDBMapperConfig)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository, dynamoDBMapperConfig)


    fun createNode(jsonString : String){
        println("Should be created in the table : $tableName")
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id

        println(node)
        repository.create(node)
    }


    fun getNode(nodeID: String): String {
        val node: Entity = repository.get(NodeIdentifier(nodeID))
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        return objectMapper.writeValueAsString(node)
    }


    fun deleteNode(nodeID : String) {
        repository.delete(NodeIdentifier(nodeID))
    }

    fun append(nodeID: String, jsonString: String) {

        val objectMapper = ObjectMapper().registerKotlinModule()
        val elements: MutableList<Element> = objectMapper.readValue(jsonString)
        println(elements)
        nodeRepository.append(nodeID, elements)

    }

    fun updateNode(jsonString: String) {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val node: Node = objectMapper.readValue(jsonString)

        /* since idCopy is SK for Node object, it can't be null if not sent from frontend */
        node.idCopy = node.id
        node.createdAt = null

        repository.update(node)
    }


    fun getAllNodesWithWorkspaceID(workspaceID : String) : MutableList<String> {

        val workspaceIdentifier  = WorkspaceIdentifier(workspaceID)
        return nodeRepository.getAllNodesWithWorkspaceID(workspaceIdentifier) as MutableList<String>
    }

    fun getAllNodesWithNamespaceID(namespaceID : String) : MutableList<String> {

        val namespaceIdentifier  = NamespaceIdentifier(namespaceID)
        return nodeRepository.getAllNodesWithNamespaceID(namespaceIdentifier) as MutableList<String>

    }



    fun jsonToObjectMapper(jsonString : String) {

        val objectMapper = ObjectMapper().registerModule(KotlinModule())

        val node: Node = objectMapper.readValue(jsonString)
        println(node)
    }


    fun jsonToElement()  {
        val jsonString = """
        {
            "type" : "AdvancedElement",
            "id": "sampleParentID",
            "namespaceIdentifier" : "1"
            "content": "Sample Content 2",
            "elementType" : "list",
            "childrenElements": [
            {
                "type" : "BasicTextElement",
                "id" : "sampleChildID",
                "content" : "sample child content"
            }
            ]
        }
        """

        val objectMapper = ObjectMapper()
        val element: Element =objectMapper.readValue(jsonString, Element::class.java)
        println(element)

    }
}

fun main(){
    val jsonString : String = """
		{
			"id": "NODE1234",
            "namespaceIdentifier" : "NAMESPACE1",
			"data": [
			{
                "type" : "AdvancedElement",
				"id": "sampleParentID",
				"content": "Sample Content",
                "elementType": "list",
                "childrenElements": [
                {
                    "type" : "BasicTextElement",
                    "id" : "sampleChildID",
                    "content" : "sample child content"
                }
                ]
			}
			],
            "createdAt": 1234,
            "updatedAt": 1234
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
            "type" : "AdvancedElement",
            "id": "sampleParentID2",
            "content": "Sample Content 2",
            "elementType" : "list",
            "childrenElements": [
            {
                "type" : "BasicTextElement",
                "id" : "sampleChildID2",
                "content" : "sample child content"
            }
            ]},
            {
            "type" : "AdvancedElement",
            "id": "sampleParentID3",
            "content": "Sample Content 3",
            "elementType" : "random element type",
            "childrenElements": [
            {
                "type" : "BasicTextElement",
                "id" : "sampleChildID3",
                "content" : "sample child content"
            }
            ]}
            
        ]
        """


    //NodeService().createNode(jsonString)
    //NodeService().getNode("NODE1234")
    //NodeService().updateNode(jsonString1)
    //NodeService().deleteNode("NODEF873GEFPVJQKV43NQMWQEJQGLF")
    //NodeService().jsonToObjectMapper(jsonString1)
    //NodeService().jsonToElement()
    //NodeService().append(jsonForAppend)
    println(System.getenv("PRIMARY_TABLE"))

    //println(NodeService().getAllNodesWithNamespaceID("NAMESPACE1"))
    //println(NodeService().getAllNodesWithWorkspaceID("WORKSPACE1"))
    //TODO("for list of nodes, I should be getting just namespace/workspace IDs and not the whole serialized object")

}


/*
   val ce : Element = BasicTextElement(
       type = "BasicTextElement",
       id = "sameBSEid",
       content = "Child Element Content"
   )
   val pe : Element = AdvancedElement(
       type = "AdvancedElement",
       id = "sampleParentID",
       parentID = "exampleID",
       content = "Sample Content",
       children = mutableListOf(ce),
       elementType = "paragraph"
   )

   val node = Node(
       id = "NODEF873GEFPVJQKV43NQMWQEJQGLF", //Helper.generateId("Node"),
       version = "xyz",
       namespaceIdentifier = NamespaceIdentifier("NAMESPACE1"),
       nodeSchemaIdentifier = NodeSchemaIdentifier(Helper.generateId(IdentifierType.NODE_SCHEMA.name)),
       workspaceIdentifier = WorkspaceIdentifier("WORKSPACE1234"),
       //status = NodeStatus.LINKED,
       data = listOf(pe),
       createdAt = 1231444
   )
   */