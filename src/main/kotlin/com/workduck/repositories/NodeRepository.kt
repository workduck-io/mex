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
		return mapper.load(Node::class.java, identifier.id)
	}

	fun append(identifier: Identifier, elements : MutableList<Element>) {
		val table = dynamoDB.getTable("elementsTableTest")

		val objectMapper = ObjectMapper()
		val elementsInStringFormat : MutableList<String> = mutableListOf()
		for(e in elements){
			val entry : String = objectMapper.writeValueAsString(e)
			elementsInStringFormat += entry
		}

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":val1"] = elementsInStringFormat
		expressionAttributeValues[":empty_list"] = mutableListOf<Element>()


		val updateItemSpec : UpdateItemSpec = UpdateItemSpec().withPrimaryKey("PK", identifier.id)
			.withUpdateExpression("set SK = list_append(if_not_exists(SK, :empty_list), :val1)")
			.withValueMap(expressionAttributeValues)


		table.updateItem(updateItemSpec)
	}


	fun getAllNodesWithNamespaceID(identifier: NamespaceIdentifier) {

		DDBHelper.getAllEntitiesWithIdentifierAndPrefix(identifier, "namespaceIdentifier",
			"namespaceIdentifier-PK-index", "Node", dynamoDB )
		//getNodesWithIdentifier(identifier, indexName = "nodesByNamespaceIndex", "namespaceIdentifier")

	}


	fun getAllNodesWithWorkspaceID(identifier: WorkspaceIdentifier) {
		DDBHelper.getAllEntitiesWithIdentifierAndPrefix(identifier, "workspaceIdentifier",
			"workspaceIdentifier-PK-index", "Node", dynamoDB )

		//getNodesWithIdentifier(identifier, indexName = "nodesByWorkspaceIndex", "workspaceIdentifier")

	}

	private fun getNodesWithIdentifier(identifier: Identifier, indexName : String, fieldName : String){


		val querySpec = QuerySpec()
		val objectMapper = ObjectMapper()
		val table: Table = dynamoDB.getTable("elementsTableTest")
		val index: Index = table.getIndex(indexName)

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":identifier"] = objectMapper.writeValueAsString(identifier)
		expressionAttributeValues[":nodePrefix"] = "Node"

		querySpec.withKeyConditionExpression(
			"$fieldName = :identifier and begins_with(PK, :nodePrefix)")
			.withValueMap(expressionAttributeValues)


		val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)
		val iterator: Iterator<Item> = items!!.iterator()

		while (iterator.hasNext()) {
			val item : Item = iterator.next()
			println(item.toJSONPretty())
		}

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