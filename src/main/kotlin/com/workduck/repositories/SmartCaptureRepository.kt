package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue
import com.workduck.models.Entity
import com.workduck.models.SmartCapture
import com.workduck.models.WorkspaceIdentifier

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

    fun updateSmartCapture(smartCapture: SmartCapture) : Entity{
        val dynamoDBMapperConfigForUpdate = DynamoDBMapperConfig.Builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()

        val saveExpression = DynamoDBSaveExpression()
        val expected = HashMap<String, ExpectedAttributeValue>()
        expected["PK"] = ExpectedAttributeValue(AttributeValue(smartCapture.workspaceIdentifier.id));
        expected["SK"] = ExpectedAttributeValue(AttributeValue(smartCapture.id))
        saveExpression.expected = expected
        saveExpression.setConditionalOperator(ConditionalOperator.AND)


        mapper.save(smartCapture, saveExpression, dynamoDBMapperConfigForUpdate )
        return smartCapture

    }

    fun deleteSmartCapture(captureID: String, workspaceID: String) {
        val table = dynamoDB.getTable(tableName)
        DeleteItemSpec().withPrimaryKey("PK", workspaceID, "SK", captureID).also { table.deleteItem(it) }
    }

    fun getSmartCapture(captureID: String, workspaceID: String): SmartCapture? {
        return mapper.load(SmartCapture::class.java, WorkspaceIdentifier(workspaceID), captureID)
    }
}