package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.UserPreferenceRecord
import com.workduck.repositories.UserPreferenceRepository
import com.workduck.utils.DDBHelper

class UserPreferenceService {

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

    private val userPreferenceRepository: UserPreferenceRepository = UserPreferenceRepository(dynamoDB, mapper, dynamoDBMapperConfig)


    fun createAndUpdateUserPreferenceRecord(jsonString: String) : UserPreferenceRecord? {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val userPreferenceRecord: UserPreferenceRecord = objectMapper.readValue(jsonString)

        return userPreferenceRepository.createAndUpdateUserPreferenceRecord(userPreferenceRecord)
    }

    fun getUserPreferenceRecord(userID : String, preferenceType : String) : UserPreferenceRecord?{
        return userPreferenceRepository.getUserPreferenceRecord(userID, preferenceType)
    }

    fun getAllUserPreferencesForUser(userID: String) : List<UserPreferenceRecord>? {
        return userPreferenceRepository.getAllUserPreferencesForUser(userID)
    }

}

fun main(){
    val json : String = """
		{
			"userID" : "USER49",
			"preferenceType" : "Sound",
			"preferenceValue" : "ta-ding"		
		}
		"""

    //UserPreferenceService().createAndUpdateUserPreferenceRecord(json)
    println(UserPreferenceService().getAllUserPreferencesForUser("USER49"))

}