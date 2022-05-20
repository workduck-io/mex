package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue
import com.serverless.utils.Constants.getCurrentTime
import com.workduck.models.Entity
import com.workduck.models.IdentifierType
import com.workduck.models.ItemStatus
import com.workduck.models.ItemType
import com.workduck.models.Snippet
import com.workduck.models.WorkspaceIdentifier
import com.workduck.utils.Helper
import com.workduck.utils.SnippetHelper.getSnippetSK
import kotlin.math.exp

class SnippetRepository(
        private val mapper: DynamoDBMapper,
        private val dynamoDB: DynamoDB,
        private val dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        private val tableName: String
) {

    fun createSnippet(snippet: Snippet) : Entity {
        val saveExpression = DynamoDBSaveExpression()
        val expected = HashMap<String, ExpectedAttributeValue>()
        expected["PK"] = ExpectedAttributeValue(false);
        expected["SK"] = ExpectedAttributeValue(false)
        saveExpression.expected = expected
        saveExpression.setConditionalOperator(ConditionalOperator.AND)
        mapper.save(snippet, saveExpression, dynamoDBMapperConfig)
        return snippet
    }

    fun updateSnippet(snippet: Snippet) : Entity{
        val dynamoDBMapperConfigForUpdate = DynamoDBMapperConfig.Builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build()

        val saveExpression = DynamoDBSaveExpression()
        val expected = HashMap<String, ExpectedAttributeValue>()
        expected["PK"] = ExpectedAttributeValue(AttributeValue(snippet.workspaceIdentifier.id));
        expected["SK"] = ExpectedAttributeValue(AttributeValue(getSnippetSK(snippet.id, snippet.version!!)))
        saveExpression.expected = expected
        saveExpression.setConditionalOperator(ConditionalOperator.AND)


        mapper.save(snippet, saveExpression, dynamoDBMapperConfigForUpdate )
        return snippet

    }



    fun getSnippetByVersion(snippetID: String, workspaceID: String, version: Int) : Entity {
        return mapper.load(Snippet::class.java, WorkspaceIdentifier(workspaceID), getSnippetSK(snippetID, version), dynamoDBMapperConfig) ?:
            throw NoSuchElementException("Not found")
    }

    fun deleteSnippetByVersion(snippetID: String, workspaceID: String, version: Int) {
        val table = dynamoDB.getTable(tableName)
        DeleteItemSpec().withPrimaryKey("PK", workspaceID, "SK", getSnippetSK(snippetID, version)).also { table.deleteItem(it) }

    }

    fun getLatestVersionNumberOfSnippet(snippetID: String, workspaceID: String): Int {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(snippetID)
        expressionAttributeValues[":itemType"] = AttributeValue().withS(ItemType.Snippet.name)
        expressionAttributeValues[":active"] = AttributeValue().withS(ItemStatus.ACTIVE.name)

        val snippetVersionList = DynamoDBQueryExpression<Snippet>().query(keyConditionExpression = "PK = :pk and begins_with(SK, :sk)",
                filterExpression = "itemType = :itemType and itemStatus = :active", expressionAttributeValues = expressionAttributeValues,
                projectionExpression = "version").let{
            mapper.query(Snippet::class.java, it, dynamoDBMapperConfig)
        }

        return snippetVersionList.maxByOrNull { it.version!! }?.version ?: throw NoSuchElementException("Snippet does not exist")

    }

    fun getAllVersionsOfSnippet(snippetID: String, workspaceID: String): List<Int?> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(snippetID)
        expressionAttributeValues[":itemType"] = AttributeValue().withS(ItemType.Snippet.name)

        return DynamoDBQueryExpression<Snippet>().query(keyConditionExpression = "PK = :pk and begins_with(SK, :sk)",
                filterExpression = "itemType = :itemType", expressionAttributeValues = expressionAttributeValues).let{
            mapper.query(Snippet::class.java, it, dynamoDBMapperConfig).map { snippet ->
                snippet.version
            }
        }
    }

    fun getAllSnippetsMetadataOfWorkspace(workspaceID: String) : List<Map<String, String>> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(IdentifierType.SNIPPET.name)
        expressionAttributeValues[":itemType"] = AttributeValue().withS(ItemType.Snippet.name)

        return DynamoDBQueryExpression<Snippet>().query(keyConditionExpression = "PK = :pk and begins_with(SK, :sk)",
                filterExpression = "itemType = :itemType", expressionAttributeValues = expressionAttributeValues,
                projectionExpression = "id, title, version").let{
            mapper.query(Snippet::class.java, it, dynamoDBMapperConfig).map { snippet ->
                mapOf("snippetID" to snippet.id, "title" to snippet.title, "version" to snippet.version.toString())
            }
        }
    }

    fun getAllSnippetsDataOfWorkspace(workspaceID: String) : List<Snippet> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":pk"] = AttributeValue().withS(workspaceID)
        expressionAttributeValues[":sk"] = AttributeValue().withS(IdentifierType.SNIPPET.name)
        expressionAttributeValues[":itemType"] = AttributeValue().withS(ItemType.Snippet.name)

        return DynamoDBQueryExpression<Snippet>().query(keyConditionExpression = "PK = :pk and begins_with(SK, :sk)",
                filterExpression = "itemType = :itemType", expressionAttributeValues = expressionAttributeValues).let{
            mapper.query(Snippet::class.java, it, dynamoDBMapperConfig)
        }
    }

    fun batchDeleteVersions(listOfSnippets : List<Snippet>){
        val failedBatches = mapper.batchWrite(emptyList<Any>(), listOfSnippets, dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

}