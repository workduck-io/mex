package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*

class NamespaceRepository(
	private val dynamoDB: DynamoDB,
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