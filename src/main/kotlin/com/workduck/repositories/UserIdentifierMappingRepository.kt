package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
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
	private val mapper: DynamoDBMapper
) : Repository<UserIdentifierRecord> {
	override fun create(t: UserIdentifierRecord): UserIdentifierRecord {
		TODO("Not yet implemented")
	}

	override fun update(t: UserIdentifierRecord): UserIdentifierRecord {
		TODO("Not yet implemented")
	}

	override fun get(identifier: Identifier): Entity {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) {
		TODO("Not yet implemented")
	}

	fun getRecordsByUserID(userID: String) {
		val table = dynamoDB.getTable("sampleData")
		val querySpec = QuerySpec()

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":userID"] = userID

		querySpec.withKeyConditionExpression("PK = :userID")
			.withValueMap(expressionAttributeValues)

		val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)
		val iterator: Iterator<Item> = items!!.iterator()

		val listOfJSON: MutableList<Any> = mutableListOf()
		while (iterator.hasNext()) {
			val item: Item = iterator.next()
			listOfJSON += item.toJSON()
			println(item.toJSONPretty())
		}

	}

	fun deleteUserIdentifierMapping(userID: String, identifier: Identifier){
		val table = dynamoDB.getTable("sampleData")

		val objectMapper = ObjectMapper()

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", userID, "SK", objectMapper.writeValueAsString(identifier))

		table.deleteItem(deleteItemSpec)
	}


}