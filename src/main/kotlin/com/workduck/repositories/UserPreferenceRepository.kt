package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.workduck.models.Identifier
import com.workduck.models.UserPreferenceRecord
import java.lang.Exception

class UserPreferenceRepository(
        private val dynamoDB: DynamoDB,
        private val mapper: DynamoDBMapper,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig
)  {

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    }
    fun createAndUpdateUserPreferenceRecord(t: UserPreferenceRecord): UserPreferenceRecord? {

        val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE)
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build()

        return try {
            mapper.save(t, dynamoDBMapperConfig)
            t
        } catch (e: Exception) {
            println(e)
            null
        }
    }

    fun updateUserPreferenceRecord(t: UserPreferenceRecord): UserPreferenceRecord? {
        TODO("Not yet implemented")
    }

    fun getUserPreferenceRecord(userID : String, preferenceType : String): UserPreferenceRecord? {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(userID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(preferenceType)
        expressionAttributeValues[":userPreferenceRecord"] = AttributeValue().withS("UserPreferenceRecord")

        val queryExpression = DynamoDBQueryExpression<UserPreferenceRecord>()
                .withKeyConditionExpression("PK = :pk and SK = :sk")
                .withFilterExpression("itemType = :userPreferenceRecord")
                .withExpressionAttributeValues(expressionAttributeValues)

        val userPreferenceList: List<UserPreferenceRecord> = mapper.query(UserPreferenceRecord::class.java, queryExpression, dynamoDBMapperConfig)

        return if(userPreferenceList.isNotEmpty()) userPreferenceList[0]
        else null
    }

    fun getAllUserPreferencesForUser(userID: String) : List<UserPreferenceRecord>?{
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(userID)
        expressionAttributeValues[":userPreferenceRecord"] = AttributeValue().withS("UserPreferenceRecord")

        val queryExpression = DynamoDBQueryExpression<UserPreferenceRecord>()
                .withKeyConditionExpression("PK = :pk")
                .withFilterExpression("itemType = :userPreferenceRecord")
                .withExpressionAttributeValues(expressionAttributeValues)

        val userPreferenceList: List<UserPreferenceRecord> = mapper.query(UserPreferenceRecord::class.java, queryExpression, dynamoDBMapperConfig)

        for(r in userPreferenceList){
            println("record : $r")
        }
        return userPreferenceList.ifEmpty { null }
    }

    fun delete(identifier: Identifier): Identifier? {
        TODO("Not yet implemented")
    }


}