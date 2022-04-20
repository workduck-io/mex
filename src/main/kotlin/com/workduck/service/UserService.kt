package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB

import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.models.requests.WDRequest

import com.workduck.models.User
import com.workduck.models.Entity
import com.workduck.models.UserIdentifier
import com.workduck.models.IdentifierType
import com.workduck.models.Identifier
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.utils.DDBHelper
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class UserService {

	private val objectMapper = Helper.objectMapper
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

	fun registerUser(workspaceName: String?): Entity?{

		val workspaceID = Helper.generateId(Helper.generateId(IdentifierType.WORKSPACE.name))

		val jsonForWorkspaceCreation = """{
			"type": "WorkspaceRequest",
			"id": "$workspaceID",
			"name": "$workspaceName"
		}"""

		val payload: WDRequest = Helper.objectMapper.readValue(jsonForWorkspaceCreation)

		return WorkspaceService().createWorkspace(payload)
	}

}
