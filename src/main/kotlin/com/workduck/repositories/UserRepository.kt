package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.User
import com.workduck.models.Workspace

class UserRepository(
	private val mapper: DynamoDBMapper
) : Repository<User>{

	override fun get(identifier: Identifier): Entity {
		return mapper.load(User::class.java, identifier.id)
	}


	override fun create(t: User): User {
		TODO("Not yet implemented")
	}

	override fun update(t: User): User {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier, tableName: String) {
		TODO("Not yet implemented")
	}

}