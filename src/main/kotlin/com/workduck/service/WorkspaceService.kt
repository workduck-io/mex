package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.WorkspaceRepository
import com.workduck.utils.DDBHelper

class WorkspaceService {

	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)

	private val tableName: String = when(System.getenv("TABLE_NAME")) {
		null -> "local-mex" /* for local testing without serverless offline */
		else -> System.getenv("TABLE_NAME")
	}

	private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
		.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
		.build()


	private val workspaceRepository: WorkspaceRepository = WorkspaceRepository(dynamoDB, mapper, dynamoDBMapperConfig)
	private val repository: Repository<Workspace> = RepositoryImpl(dynamoDB, mapper, workspaceRepository, dynamoDBMapperConfig)

	fun createWorkspace(jsonString : String) : Workspace? {
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val workspace: Workspace = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		workspace.idCopy = workspace.id

		return repository.create(workspace)
	}

	fun getWorkspace(workspaceID : String) : String? {
		val workspace: Entity = repository.get(WorkspaceIdentifier(workspaceID))?: return null
		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		return objectMapper.writeValueAsString(workspace)
	}


	fun updateWorkspace(jsonString: String) : Workspace? {

		val objectMapper = ObjectMapper().registerModule(KotlinModule())
		val workspace: Workspace = objectMapper.readValue(jsonString)

		/* since idCopy is SK for Namespace object, it can't be null if not sent from frontend */
		workspace.idCopy = workspace.id

		/* to avoid updating createdAt un-necessarily */
		workspace.createdAt = null

		return repository.update(workspace)
	}

	fun deleteWorkspace(workspaceID: String) : String? {
		return repository.delete(WorkspaceIdentifier(workspaceID))
	}

	fun getWorkspaceData(workspaceIDList : List<String>) : MutableList<String>? {
		return workspaceRepository.getWorkspaceData(workspaceIDList)
	}


}


fun main() {
	val json : String = """
		{
			"id": "WORKSPACE1",
			"name": "WorkDuck"
		}
		"""

	val jsonUpdate : String = """
		{
			"id" : "WORKSPACE1",
			"name" : "WorkDuck Pvt. Ltd."
		}
		"""
	//WorkspaceService().createWorkspace(json)
	//WorkspaceService().updateWorkspace(jsonUpdate)
	//WorkspaceService().deleteWorkspace("WORKSPACE1")
	println(WorkspaceService().getWorkspaceData(mutableListOf("WORKSPACE1")))

}