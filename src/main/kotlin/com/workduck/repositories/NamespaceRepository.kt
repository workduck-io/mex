package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*

class NamespaceRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper

) : Repository<Namespace> {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Namespace::class.java, identifier.id)
	}

	fun getAllNodesWithNamespaceID(identifier: NamespaceIdentifier, tableName: String) {

		val table: Table = dynamoDB.getTable(tableName)
		val index: Index = table.getIndex("nodesByNamespaceIndex")

		val querySpec = QuerySpec()

		val objectMapper = ObjectMapper()

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":namespaceIdentifier"] = objectMapper.writeValueAsString(identifier)
		expressionAttributeValues[":nodePrefix"] = "Node"

		querySpec.withKeyConditionExpression(
			"namespaceIdentifier = :namespaceIdentifier and begins_with(PK, :nodePrefix)")
			.withValueMap(expressionAttributeValues)

		val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)

		val iterator: Iterator<Item> = items!!.iterator()

		while (iterator.hasNext()) {
			val item : Item = iterator.next()
			println(item.toJSONPretty())
		}

	}



	override fun create(t: Namespace): Namespace {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier, tableName: String) {
		TODO("Not yet implemented")
	}

	override fun update(t: Namespace): Namespace {
		TODO("Not yet implemented")
	}


}