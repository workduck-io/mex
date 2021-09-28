package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.*
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

	fun getAllNodesWithWorkspaceID(identifier: WorkspaceIdentifier, tableName: String) {

		val table: Table = dynamoDB.getTable(tableName)
		val index: Index = table.getIndex("nodesByWorkspaceIndex")

		val querySpec = QuerySpec()

		val objectMapper = ObjectMapper()

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":workspaceIdentifier"] = objectMapper.writeValueAsString(identifier)
		expressionAttributeValues[":nodePrefix"] = "Node"

		querySpec.withKeyConditionExpression(
			"workspaceIdentifier = :workspaceIdentifier and begins_with(PK, :nodePrefix)")
			.withValueMap(expressionAttributeValues)

		val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)

		val iterator: Iterator<Item> = items!!.iterator()

		while (iterator.hasNext()) {
			val item : Item = iterator.next()
			println(item.toJSONPretty())
		}

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



}