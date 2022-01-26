package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.WDRequest
import com.serverless.models.requests.WorkspaceRequest
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.WorkspaceRepository
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class WorkspaceService {

    private val objectMapper = Helper.objectMapper
    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
    private val dynamoDB: DynamoDB = DynamoDB(client)
    private val mapper = DynamoDBMapper(client)

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }

    private val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    private val workspaceRepository: WorkspaceRepository = WorkspaceRepository(dynamoDB, mapper, dynamoDBMapperConfig)
    private val repository: Repository<Workspace> = RepositoryImpl(dynamoDB, mapper, workspaceRepository, dynamoDBMapperConfig)

    fun createWorkspace(workspaceRequest: WDRequest?): Entity? {
        val workspace : Workspace = createWorkspaceObjectFromWorkspaceRequest(workspaceRequest as WorkspaceRequest?) ?: return null

        LOG.info("Creating workspace : $workspace")
        return repository.create(workspace)
    }

    fun getWorkspace(workspaceID: String): Entity? {
        LOG.info("Getting workspace with id : $workspaceID")
        return repository.get(WorkspaceIdentifier(workspaceID))
    }

    fun updateWorkspace(workspaceRequest: WDRequest?): Entity? {
        val workspace: Workspace = createWorkspaceObjectFromWorkspaceRequest(workspaceRequest as WorkspaceRequest?) ?: return null

        workspace.createdAt = null

        LOG.info("Updating workspace : $workspace")
        return repository.update(workspace)
    }

    fun deleteWorkspace(workspaceID: String): Identifier? {
        LOG.info("Deleting workspace with id : $workspaceID")
        return repository.delete(WorkspaceIdentifier(workspaceID))
    }

    fun getWorkspaceData(workspaceIDList: List<String>): MutableMap<String, Workspace?>? {
        LOG.info("Getting workspaces with ids : $workspaceIDList")
        return workspaceRepository.getWorkspaceData(workspaceIDList)
    }

    private fun createWorkspaceObjectFromWorkspaceRequest(workspaceRequest : WorkspaceRequest?) : Workspace?{
        return workspaceRequest?.let {
            Workspace(id = workspaceRequest.id,
                    name = workspaceRequest.name)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceService::class.java)
    }
}

fun main() {
    val json: String = """
		{
			"id": "WORKSPACE1",
			"name": "WorkDuck"
		}
		"""

    val jsonUpdate: String = """
		{
			"id" : "WORKSPACE1",
			"name" : "WorkDuck Pvt. Ltd."
		}
		"""
    // WorkspaceService().createWorkspace(json)
    // WorkspaceService().updateWorkspace(jsonUpdate)
    // WorkspaceService().deleteWorkspace("WORKSPACE1")
    //println(WorkspaceService().getWorkspaceData(mutableListOf("WORKSPACE1")))
}
