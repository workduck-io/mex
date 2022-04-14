package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.ItemType
import com.workduck.models.Snippet
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.SnippetHelper.getSnippetSK

class SnippetRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName: String
) {

    fun getSnippetByVersion(snippetID: String, workspaceID: String, version: Long) : Entity {
        return mapper.load(Snippet::class.java, WorkspaceIdentifier(workspaceID), getSnippetSK(snippetID, version), dynamoDBMapperConfig) ?:
            throw NoSuchElementException("Not found")
    }

    fun getLatestVersionNumberOfSnippet(snippetID: String, workspaceID: String): Long {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(snippetID)
        expressionAttributeValues[":itemType"] = AttributeValue().withS(ItemType.Snippet.name)

        val nodeVersionList: List<Snippet> = DynamoDBQueryExpression<Snippet>()
                .withKeyConditionExpression("PK = :pk and begins_with(SK, :sk)")
                .withFilterExpression("itemType = :itemType")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withProjectionExpression("version").let{
                    mapper.query(Snippet::class.java, it, dynamoDBMapperConfig)
                }


        return nodeVersionList.maxByOrNull { it.version!! }?.version ?: throw NoSuchElementException("Snippet does not exist")

    }

    fun getAllVersionsOfSnippet(snippetID: String, workspaceID: String): List<Entity> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(snippetID)
        expressionAttributeValues[":itemType"] = AttributeValue().withS(ItemType.Snippet.name)

        return DynamoDBQueryExpression<Snippet>()
                .withKeyConditionExpression("PK = :pk and begins_with(SK, :sk)")
                .withFilterExpression("itemType = :itemType")
                .withExpressionAttributeValues(expressionAttributeValues)
                .let{
                    mapper.query(Snippet::class.java, it, dynamoDBMapperConfig) ?: listOf()
                }

    }

}