package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.serverless.models.requests.UserPreferenceRequest
import com.serverless.models.requests.WDRequest
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

    fun createAndUpdateUserPreferenceRecord(userPreferenceRequest: WDRequest) {
        val userPreferenceRecord: UserPreferenceRecord = convertUserPreferenceRequestToUserPreferenceObject(userPreferenceRequest as UserPreferenceRequest)

        userPreferenceRepository.createAndUpdateUserPreferenceRecord(userPreferenceRecord)
    }

    fun getUserPreferenceRecord(userID: String, preferenceType: String): UserPreferenceRecord? {
        return userPreferenceRepository.getUserPreferenceRecord(userID, preferenceType)
    }

    fun getAllUserPreferencesForUser(userID: String): List<UserPreferenceRecord>? {
        return userPreferenceRepository.getAllUserPreferencesForUser(userID)
    }

    private fun convertUserPreferenceRequestToUserPreferenceObject(userPreferenceRequest: UserPreferenceRequest): UserPreferenceRecord {
        return UserPreferenceRecord(
            userID = userPreferenceRequest.userID,
            preferenceType = userPreferenceRequest.preferenceType,
            preferenceValue = userPreferenceRequest.preferenceValue
        )
    }
}