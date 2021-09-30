package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier


class WorkspaceRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper

) : Repository<Workspace> {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Workspace::class.java, identifier.id)
	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable("sampleData")

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		table.deleteItem(deleteItemSpec)
	}

	override fun create(t: Workspace): Workspace {
		TODO("Not yet implemented")
	}

	override fun update(t: Workspace): Workspace {
		TODO("Not yet implemented")
	}


}