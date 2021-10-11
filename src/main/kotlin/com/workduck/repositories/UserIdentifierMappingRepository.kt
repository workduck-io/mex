package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.UserIdentifierRecord

class UserIdentifierMappingRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<UserIdentifierRecord> {

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	override fun create(t: UserIdentifierRecord): UserIdentifierRecord {
		TODO("Not yet implemented")
	}

	override fun update(t: UserIdentifierRecord): UserIdentifierRecord {
		TODO("Not yet implemented")
	}

	override fun get(identifier: Identifier): Entity {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier): Identifier? {
		TODO("Not yet implemented")
	}

	fun getRecordsByUserID(userID: String) : MutableList<String> {
		val table = dynamoDB.getTable(tableName)
		val querySpec = QuerySpec()

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":userID"] = userID

		querySpec.withKeyConditionExpression("PK = :userID")
			.withValueMap(expressionAttributeValues)

		val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)
		val iterator: Iterator<Item> = items!!.iterator()

		val listOfJSON: MutableList<String> = mutableListOf()
		while (iterator.hasNext()) {
			val item: Item = iterator.next()
			listOfJSON += item.toJSON()
		}
		return listOfJSON

	}

	fun deleteUserIdentifierMapping(userID: String, identifier: Identifier) : Map<String, String>? {
		val table = dynamoDB.getTable(tableName)

		val objectMapper = ObjectMapper()

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", userID, "SK", objectMapper.writeValueAsString(identifier))

		return try {
			table.deleteItem(deleteItemSpec)
			mapOf("userID" to userID, "identifierID"  to identifier.id)
		} catch ( e : Exception) {
			null
		}
	}


}