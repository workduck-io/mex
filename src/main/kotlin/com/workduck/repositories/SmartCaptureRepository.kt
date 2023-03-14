package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue
import com.workduck.models.Entity
import com.workduck.models.SmartCapture

class SmartCaptureRepository(
    private val mapper: DynamoDBMapper,
    private val dynamoDB: DynamoDB,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig,
    private val client: AmazonDynamoDB,
    private val tableName: String
    ) {
    fun createSmartCapture(smartCapture: SmartCapture) : Entity {
        val saveExpression = DynamoDBSaveExpression()
        val expected = HashMap<String, ExpectedAttributeValue>()
        expected["PK"] = ExpectedAttributeValue(false);
        expected["SK"] = ExpectedAttributeValue(false)
        saveExpression.expected = expected
        saveExpression.setConditionalOperator(ConditionalOperator.AND)
        mapper.save(smartCapture, saveExpression, dynamoDBMapperConfig)
        return smartCapture
    }
}