package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*

class UserRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper
) : Repository<User>{

	override fun get(identifier: Identifier): Entity {
		return mapper.load(User::class.java, identifier.id)
	}

	fun getAllUsersWithNamespaceID(identifier: NamespaceIdentifier){
		val querySpec = QuerySpec()
		val objectMapper = ObjectMapper()
		val table: Table = dynamoDB.getTable("sampleData")
		val index: Index = table.getIndex("namespaceIdentifier-PK-index")

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":identifier"] = objectMapper.writeValueAsString(identifier)
		expressionAttributeValues[":userPrefix"] = "USER"

		querySpec.withKeyConditionExpression(
			"namespaceIdentifier = :identifier and begins_with(PK, :userPrefix)")
			.withValueMap(expressionAttributeValues)

		val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)
		val iterator: Iterator<Item> = items!!.iterator()

		while (iterator.hasNext()) {
			val item : Item = iterator.next()
			println(item.toJSONPretty())
		}
	}


	fun getAllUsersWithWorkspaceID(identifier: WorkspaceIdentifier){
		val querySpec = QuerySpec()
		val objectMapper = ObjectMapper()
		val table: Table = dynamoDB.getTable("sampleData")
		val index: Index = table.getIndex("workspaceIdentifier-PK-index")

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":identifier"] = objectMapper.writeValueAsString(identifier)
		expressionAttributeValues[":userPrefix"] = "USER"

		querySpec.withKeyConditionExpression(
			"workspaceIdentifier = :identifier and begins_with(PK, :userPrefix)")
			.withValueMap(expressionAttributeValues)

		val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)
		val iterator: Iterator<Item> = items!!.iterator()

		while (iterator.hasNext()) {
			val item : Item = iterator.next()
			println(item.toJSONPretty())
		}
	}

	override fun create(t: User): User {
		TODO("Not yet implemented")
	}

	override fun update(t: User): User {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) {
		TODO("Not yet implemented")
	}

}