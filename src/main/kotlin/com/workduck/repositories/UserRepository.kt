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

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	override fun get(identifier: Identifier): Entity {
		return mapper.load(User::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
	}

//	fun getAllUsersWithNamespaceID(identifier: NamespaceIdentifier): MutableList<String> {
//
//		return DDBHelper.getAllEntitiesWithIdentifierAndPrefix(
//			identifier, "SK",
//			"SK-PK-index", "USER", dynamoDB
//		)
//
//	}
//
//
//	fun getAllUsersWithWorkspaceID(identifier: WorkspaceIdentifier): MutableList<String> {
//
//		return DDBHelper.getAllEntitiesWithIdentifierAndPrefix(
//			identifier, "SK",
//			"SK-PK-index", "USER", dynamoDB
//		)
//
//	}

	override fun create(t: User): User {
		TODO("Not yet implemented")
	}

	override fun update(t: User): User {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable(tableName)

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		table.deleteItem(deleteItemSpec)
	}

}