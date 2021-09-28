package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.workduck.models.*

class WorkspaceRepository(
	private val mapper: DynamoDBMapper

) : Repository<Workspace> {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Workspace::class.java, identifier.id)
	}

	override fun create(t: Workspace): Workspace {
		TODO("Not yet implemented")
	}

	override fun delete(identifier: Identifier, tableName: String) {
		TODO("Not yet implemented")
	}

	override fun update(t: Workspace): Workspace {
		TODO("Not yet implemented")
	}

	override fun append(identifier: Identifier, tableName: String, elements: MutableList<Element>) {
		TODO("Not yet implemented")
	}

}