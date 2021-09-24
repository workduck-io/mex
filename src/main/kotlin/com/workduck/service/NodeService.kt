package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper


/**
 * contains all node related logic
 */
class NodeService {
    //Todo: Inject them from handlers

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    var dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)
    private val repository: Repository<Node> = RepositoryImpl(dynamoDB, mapper)


    fun createNode(){

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
            childrenElements = listOf(ce),
            elementType = ElementTypes.PARAGRAPH
        )

        val node = Node(
            id = Helper.generateId("Node"),
            version = "xyz",
            namespaceIdentifier = NamespaceIdentifier(Helper.generateId(IdentifierType.NAMESPACE.name)),
            nodeSchemaIdentifier = NodeSchemaIdentifier(Helper.generateId(IdentifierType.NODE_SCHEMA.name)),
            //status = NodeStatus.LINKED,
            data = listOf(pe),
            createdAt = 1231444
        )

        repository.create(node)
    }


    fun getNode() {
        repository.get(NodeIdentifier("NodeFPGT8M5ES5828ZGKE9VFF7DS9V"))
    }
    fun deleteNode() {

        repository.delete(NodeIdentifier("abc"))
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

        var dynamoDB: DynamoDB = DynamoDB(client)

        var table: Table = dynamoDB.getTable("usersTable")

       // val mapValues: Map<String, AttributeValue> = HashMap()
        val item : Item = Item()
            .withPrimaryKey("email", "varun.iitp@gmail.com")
            .withString("name", "VarunGarg")

        val outcome = table.putItem(item)
        //private val repository: Repository<Node> = RepositoryImpl(dynamoDB);

    }

}

fun main(){
     NodeService().getNode()
    // NodeService().createNode()
   // NodeService().jsonToObjectMapper()
}