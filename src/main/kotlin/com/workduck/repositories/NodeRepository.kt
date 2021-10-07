package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*
import com.workduck.utils.DDBHelper

class NodeRepository(
	private val mapper: DynamoDBMapper,
	private val dynamoDB: DynamoDB,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<Node>  {

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Node::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
	}

	fun append(nodeID : String, elements : MutableList<Element>) {
		val table = dynamoDB.getTable(tableName)

		val objectMapper = ObjectMapper()
		val elementsInStringFormat : MutableList<String> = mutableListOf()
		for(e in elements){
			val entry : String = objectMapper.writeValueAsString(e)
			elementsInStringFormat += entry
		}

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":val1"] = elementsInStringFormat
		expressionAttributeValues[":empty_list"] = mutableListOf<Element>()


		val updateItemSpec : UpdateItemSpec = UpdateItemSpec().withPrimaryKey("PK", nodeID, "SK", nodeID)
			.withUpdateExpression("set nodeData = list_append(if_not_exists(nodeData, :empty_list), :val1)")
			.withValueMap(expressionAttributeValues)


		table.updateItem(updateItemSpec)
	}


	fun getAllNodesWithNamespaceID(namespaceID: String, workspaceID: String): MutableList<String> {

		val akValue = "$workspaceID#$namespaceID"
		return DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(akValue, "itemType-AK-index", dynamoDB, "Node")

	}

	fun getAllNodesWithWorkspaceID(workspaceID: String): MutableList<String> {

		return DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(workspaceID, "itemType-AK-index", dynamoDB, "Node")

	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable(tableName)

		val deleteItemSpec : DeleteItemSpec =  DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		table.deleteItem(deleteItemSpec)
	}


	override fun create(t: Node): Node {
		TODO("Not yet implemented")
	}

	override fun update(t: Node): Node {
		TODO("Not yet implemented")
	}

}