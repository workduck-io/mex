package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
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
	private val mapper: DynamoDBMapper,
	private val dynamoDBMapperConfig: DynamoDBMapperConfig

) : Repository<Workspace> {

	override fun get(identifier: Identifier): Entity {
		return mapper.load(Workspace::class.java, identifier.id, identifier.id)
	}

	override fun delete(identifier: Identifier) {
		val table = dynamoDB.getTable(System.getenv("TABLE_NAME"))

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

	fun getWorkspaceData(workspaceIDList : List<String>) : MutableList<String>{
		val workspaceJsonList : MutableList<String>  = mutableListOf()
		val objectMapper = ObjectMapper()
		for(workspaceID in workspaceIDList ) {
			val workspace : Workspace? = mapper.load(Workspace::class.java, workspaceID, workspaceID, dynamoDBMapperConfig)
			if(workspace!=null) {
				val workspaceJson = objectMapper.writeValueAsString(workspace)
				workspaceJsonList += workspaceJson
			}
		}
		return workspaceJsonList
		TODO("we also need to have some sort of filter which filters out all the non-workspace ids")
	}


}