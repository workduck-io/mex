package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*
import com.workduck.utils.DDBHelper

class NodeRepository(
	private val mapper: DynamoDBMapper,
	private val dynamoDB: DynamoDB
) : Repository<Node>  {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Node::class.java, identifier.id, identifier.id)
	}

	fun append(identifier: Identifier, elements : MutableList<Element>) {
		val table = dynamoDB.getTable("sampleData")

		val objectMapper = ObjectMapper()
		val elementsInStringFormat : MutableList<String> = mutableListOf()
		for(e in elements){
			val entry : String = objectMapper.writeValueAsString(e)
			elementsInStringFormat += entry
		}

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":val1"] = elementsInStringFormat
		expressionAttributeValues[":empty_list"] = mutableListOf<Element>()


		val updateItemSpec : UpdateItemSpec = UpdateItemSpec().withPrimaryKey("PK", identifier.id, "SK", identifier.id)
			.withUpdateExpression("set nodeData = list_append(if_not_exists(nodeData, :empty_list), :val1)")
			.withValueMap(expressionAttributeValues)


		table.updateItem(updateItemSpec)
	}


	fun getAllNodesWithNamespaceID(identifier: NamespaceIdentifier) : MutableList<String> {

		return DDBHelper.getAllEntitiesWithIdentifierAndPrefix(identifier, "namespaceIdentifier",
			"namespaceIdentifier-PK-index", "NODE", dynamoDB )

	}


	fun getAllNodesWithWorkspaceID(identifier: WorkspaceIdentifier) : MutableList<String> {

		return DDBHelper.getAllEntitiesWithIdentifierAndPrefix(identifier, "workspaceIdentifier",
			"workspaceIdentifier-PK-index", "NODE", dynamoDB )

	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable("sampleData")

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