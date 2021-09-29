package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.workduck.models.*
import com.workduck.repositories.NodeRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.WorkspaceRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import java.util.*


/**
 * contains all node related logic
 */
class NodeService {
    //Todo: Inject them from handlers

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)
    private val nodeRepository : NodeRepository = NodeRepository(mapper, dynamoDB)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper, nodeRepository)


    fun createNode(){

        val ce : Element = BasicTextElement(
            type = "BasicTextElement",
            id = "sameBSEid",
            content = "Child Element Content",
        )
        val pe : Element = AdvancedElement(
            type = "AdvancedElement",
            id = "sampleParentID",
            parentID = "exampleID",
            content = "Sample Content",
            children = mutableListOf(ce),
            elementType = "paragraph",
        )

        val node = Node(
            id = "NodeF873GEFPVJQKV43NQMWQEJQGLF", //Helper.generateId("Node"),
            version = "xyz",
            namespaceIdentifier = NamespaceIdentifier("NAMESPACE1"),
            nodeSchemaIdentifier = NodeSchemaIdentifier(Helper.generateId(IdentifierType.NODE_SCHEMA.name)),
            workspaceIdentifier = WorkspaceIdentifier("WORKSPACE1234"),
            //status = NodeStatus.LINKED,
            data = listOf(pe),
            createdAt = 1231444
        )

        repository.create(node)
    }


    fun getNode() {

        val node : Entity = repository.get(NodeIdentifier("NodeF873GEFPVJQKV43NQMWQEJQGLF"))
        println(node)
    }
    fun deleteNode() {

        repository.delete(NodeIdentifier("NodeF873GEFPVJQKV43NQMWQEJQGLF"))
    }

    fun append() {


        val jsonString = """
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

        val objectMapper = ObjectMapper().registerKotlinModule()
        val elements: MutableList<Element> = objectMapper.readValue(jsonString)
        println(elements)
        nodeRepository.append(NodeIdentifier("NodeF873GEFPVJQKV43NQMWQEJQGLF"), elements)

    }

    fun updateNode() {
        val ce : Element = BasicTextElement(
            type = "BasicTextElement",
            id = "sameBSEid",
            content = "Child Element Content",
        )
        val pe : Element = AdvancedElement(
            type = "AdvancedElement",
            id = "sampleParentID",
            parentID = "exampleID",
            content = "Sample Content",
            children = listOf(ce),
            elementType = "list",
        )

        val node = Node(
            id = "NodeF873GEFPVJQKV43NQMWQEJQGLF", //Helper.generateId("Node"),
            version = "xyz",
            namespaceIdentifier = NamespaceIdentifier(Helper.generateId(IdentifierType.NAMESPACE.name)),
            nodeSchemaIdentifier = NodeSchemaIdentifier(Helper.generateId(IdentifierType.NODE_SCHEMA.name)),
            workspaceIdentifier = WorkspaceIdentifier("WS1234"),
            //status = NodeStatus.LINKED,
            data = listOf(pe),
            createdAt = 1231444
        )
        repository.update(node)
    }


    fun getAllNodesWithWorkspaceID(){
        val workspaceID = "WS1234"
        val workspaceIdentifier : WorkspaceIdentifier = WorkspaceIdentifier(workspaceID)
        nodeRepository.getAllNodesWithWorkspaceID(workspaceIdentifier)
    }

    fun getAllNodesWithNamespaceID(){
        val namespaceID = "NAMESPACELH65W9RM3BQ62FLLFEST1PSHDQ"
        val namespaceIdentifier : NamespaceIdentifier = NamespaceIdentifier(namespaceID)
        nodeRepository.getAllNodesWithNamespaceID(namespaceIdentifier)
    }



    fun jsonToObjectMapper() {
        val jsonString = """
		{
			"id": "NODE1234",
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

        val objectMapper = ObjectMapper()
        val node: Node = objectMapper.readValue(jsonString, Node::class.java)
        println(node)
    }


    fun connectToDynamo() {
        val client: AmazonDynamoDB = DDBHelper.createDDBConnection()

        val dynamoDB: DynamoDB = DynamoDB(client)

        val table: Table = dynamoDB.getTable("usersTable")

       // val mapValues: Map<String, AttributeValue> = HashMap()
        val item : Item = Item()
            .withPrimaryKey("email", "varun.iitp@gmail.com")
            .withString("name", "VarunGarg")

        val outcome = table.putItem(item)
        //private val repository: Repository<Node> = RepositoryImpl(dynamoDB);

    }

    fun jsonToElement()  {
        val jsonString = """
        {
            "type" : "AdvancedElement",
            "id": "sampleParentID",
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
    NodeService().createNode()
    // NodeService().getNode()
    // NodeService().updateNode()
    //NodeService().deleteNode()
    //NodeService().jsonToObjectMapper()
    //NodeService().jsonToElement()
    //NodeService().append()

    //NodeService().getAllNodesWithNamespaceID()
    //NodeService().getAllNodesWithWorkspaceID()


}
