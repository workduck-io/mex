package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.workduck.models.*

class NamespaceRepository(
	private val mapper: DynamoDBMapper

) : Repository<Namespace> {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Namespace::class.java, identifier.id)
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