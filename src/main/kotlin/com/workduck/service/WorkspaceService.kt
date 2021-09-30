package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.models.WorkspaceIdentifier
import com.workduck.repositories.NamespaceRepository
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.WorkspaceRepository
import com.workduck.utils.DDBHelper

class WorkspaceService {

	private val client: AmazonDynamoDB = DDBHelper.createDDBConnection()
	private val dynamoDB: DynamoDB = DynamoDB(client)
	private val mapper = DynamoDBMapper(client)
	private val workspaceRepository: WorkspaceRepository = WorkspaceRepository(dynamoDB, mapper)
	private val repository: Repository<Workspace> = RepositoryImpl(dynamoDB, mapper, workspaceRepository)

	fun createWorkspace() {
		val ws: Workspace = Workspace(
			id = "WORKSPACE1234",
			name = "WorkDuck"
		)
		repository.create(ws)
	}

	fun getWorkspace() {
		val workspace: Entity = repository.get(WorkspaceIdentifier("WS1234"))
		println(workspace)
	}


	fun updateWorkspace() {
		val ws: Workspace = Workspace(
			id = "WORKSPACE1234",
			name = "WorkDuck Pvt Ltd"
		)

		repository.update(ws)
	}

	fun deleteWorkspace() {
		repository.delete(WorkspaceIdentifier("WORKSPACE1234"))
	}


}


fun main() {
	WorkspaceService().createWorkspace()
	//WorkspaceService().updateWorkspace()
	//WorkspaceService().deleteWorkspace()
	//WorkspaceService().getWorkspace()

}