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

	override fun get(identifier: Identifier): Entity? {
		return try {
			mapper.load(User::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
		} catch (e : Exception) {
			null
		}
	}

	fun getAllUsersWithNamespaceID(namespaceID: String): MutableList<String>? {

		return try {
			DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(namespaceID, "itemType-AK-index", dynamoDB, "UserIdentifierRecord")
		} catch( e: Exception){
			null
		}

	}


	fun getAllUsersWithWorkspaceID(workspaceID: String): MutableList<String>? {

		return try {
			DDBHelper.getAllEntitiesWithIdentifierIDAndPrefix(workspaceID, "itemType-AK-index", dynamoDB, "UserIdentifierRecord")
		} catch( e: Exception) {
			null
		}

	}

	override fun create(t: User): User {
		TODO("Not yet implemented")
	}

	override fun update(t: User): User {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier) : String? {
		val table = dynamoDB.getTable(tableName)

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		return try {
			table.deleteItem(deleteItemSpec)
			identifier.id
		} catch( e: Exception){
			null
		}
	}

}