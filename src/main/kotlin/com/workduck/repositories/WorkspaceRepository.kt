package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.*


class WorkspaceRepository(
	private val dynamoDB: DynamoDB,
	private val mapper: DynamoDBMapper,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Workspace> {

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	override fun get(identifier: Identifier): Entity? {
		return try {
			return mapper.load(Workspace::class.java, identifier.id, identifier.id, dynamoDBMapperConfig)
		} catch (e : Exception){
			null
		}
	}

	override fun delete(identifier: Identifier) : String? {
		val table = dynamoDB.getTable(tableName)

		val deleteItemSpec: DeleteItemSpec = DeleteItemSpec()
			.withPrimaryKey("PK", identifier.id, "SK", identifier.id)

		return try {
			table.deleteItem(deleteItemSpec)
			identifier.id
		} catch ( e : Exception){
			null
		}
	}

	override fun create(t: Workspace): Workspace {
		TODO("Not yet implemented")
	}

	override fun update(t: Workspace): Workspace {
		TODO("Not yet implemented")
	}

	fun getWorkspaceData(workspaceIDList : List<String>) : MutableList<String>? {
		val workspaceJsonList : MutableList<String>  = mutableListOf()
		val objectMapper = ObjectMapper()
		return try {
			for (workspaceID in workspaceIDList) {
				val workspace: Workspace? =
					mapper.load(Workspace::class.java, workspaceID, workspaceID, dynamoDBMapperConfig)
				if (workspace != null) {
					val workspaceJson = objectMapper.writeValueAsString(workspace)
					workspaceJsonList += workspaceJson
				}
			}
			workspaceJsonList
		} catch ( e : Exception) {
			null
		}
		TODO("we also need to have some sort of filter which filters out all the non-workspace ids")
	}


}