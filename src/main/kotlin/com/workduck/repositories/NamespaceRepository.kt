package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.workduck.models.Element
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Namespace

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

	override fun append(identifier: Identifier, tableName: String, elements: MutableList<Element>) {
		TODO("Not yet implemented")
	}

}