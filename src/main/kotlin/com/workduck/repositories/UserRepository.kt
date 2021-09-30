package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
import com.workduck.models.*
import com.workduck.utils.DDBHelper

class UserRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper
) : Repository<User>{

	override fun get(identifier: Identifier): Entity {
		return mapper.load(User::class.java, identifier.id)
	}

	fun getAllUsersWithNamespaceID(identifier: NamespaceIdentifier){

		DDBHelper.getAllEntitiesWithIdentifierAndPrefix(identifier, "namespaceIdentifier",
			"namespaceIdentifier-PK-index", "USER", dynamoDB )

	}


	fun getAllUsersWithWorkspaceID(identifier: WorkspaceIdentifier){

		DDBHelper.getAllEntitiesWithIdentifierAndPrefix(identifier, "workspaceIdentifier",
			"workspaceIdentifier-PK-index", "USER", dynamoDB )

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