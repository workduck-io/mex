package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.*
import com.workduck.repositories.Repository
import com.workduck.repositories.RepositoryImpl
import com.workduck.repositories.UserIdentifierMappingRepository
import com.workduck.utils.DDBHelper

class UserIdentifierMappingService {
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

    private val userIdentifierMappingRepository: UserIdentifierMappingRepository = UserIdentifierMappingRepository(dynamoDB, mapper, dynamoDBMapperConfig)
    private val repository: Repository<UserIdentifierRecord> = RepositoryImpl(dynamoDB, mapper, userIdentifierMappingRepository, dynamoDBMapperConfig)

    fun createUserIdentifierRecord(jsonString: String): Entity? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val userIdentifierRecord: UserIdentifierRecord = objectMapper.readValue(jsonString)

        return repository.create(userIdentifierRecord)
    }

    /* returns user details data + user mapping with namespace + user mapping with workspace */
    fun getUserRecords(userID: String): MutableList<String>? {
        return try {
            userIdentifierMappingRepository.getRecordsByUserID(userID)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteUserIdentifierMapping(userID: String, identifierID: String): Map<String, String>? {
        return if (identifierID.startsWith("NAMESPACE"))
            userIdentifierMappingRepository.deleteUserIdentifierMapping(userID, NamespaceIdentifier(identifierID))
        else
            userIdentifierMappingRepository.deleteUserIdentifierMapping(userID, WorkspaceIdentifier(identifierID))
    }
}

fun main() {
    val json: String = """
		{
			"userID" : "USER49",
			"identifier" : "NAMESPACE1"
		}
		"""
    // UserIdentifierMappingService().createUserIdentifierRecord(json)
    println(UserIdentifierMappingService().getUserRecords("USER49").toString())
    // UserIdentifierMappingService().deleteUserIdentifierMapping("USER49", "NAMESPACE1")
}
