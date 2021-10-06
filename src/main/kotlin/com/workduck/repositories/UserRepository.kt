package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*
import com.workduck.utils.DDBHelper

class UserRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig
) : Repository<User> {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(User::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
	}

	fun getAllUsersWithNamespaceID(identifier: NamespaceIdentifier): MutableList<String> {

		return DDBHelper.getAllEntitiesWithIdentifierAndPrefix(
			identifier, "SK",
			"SK-PK-index", "USER", dynamoDB
		)

	}


	fun getAllUsersWithWorkspaceID(identifier: WorkspaceIdentifier): MutableList<String> {

		return DDBHelper.getAllEntitiesWithIdentifierAndPrefix(
			identifier, "SK",
			"SK-PK-index", "USER", dynamoDB
		)

	}

//	fun getAllUsersWithIdentifier(identifier: Identifier) : MutableList<String>{
//		val querySpec = QuerySpec()
//		val objectMapper = ObjectMapper()
//		val table: Table = dynamoDB.getTable("sampleData")
//		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
//		expressionAttributeValues[":identifier"] = objectMapper.writeValueAsString(identifier)
//
//		/* only time we have SK as identifier is when we add user-identifier mapping record */
//		querySpec.withKeyConditionExpression(
//			"SK = :identifier" and
//		)
//			.withValueMap(expressionAttributeValues)
//
//		val items: ItemCollection<QueryOutcome?>? = table.query(querySpec)
//		val iterator: Iterator<Item> = items!!.iterator()
//
//		val listOfJSON: MutableList<String> = mutableListOf()
//		while (iterator.hasNext()) {
//			val item: Item = iterator.next()
//			listOfJSON += item.toJSON()
//			//println(item.toJSONPretty())
//		}
//
//		return listOfJSON
//
//
//	}

	override fun create(t: User): User {
		TODO("Not yet implemented")
	}

	override fun update(t: User): User {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable(System.getenv("TABLE_NAME"))

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		table.deleteItem(deleteItemSpec)
	}

}