package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.utils.Constants
import com.workduck.models.ItemStatus
import com.workduck.models.Relationship
import com.workduck.models.RelationshipType
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class RelationshipRepository(
    private val mapper: DynamoDBMapper,
    var dynamoDBMapperConfig: DynamoDBMapperConfig,
    private val dynamoDB: DynamoDB,
    var tableName: String
) {

    fun createInBatch(list: List<Relationship>) {
        val failedBatches = mapper.batchWrite(list, emptyList<Any>(), dynamoDBMapperConfig)
        Helper.logFailureForBatchOperation(failedBatches)
    }

    fun getHierarchyRelationshipsOfWorkspace(workspaceID: String, status: ItemStatus): List<Relationship> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":workspaceIdentifier"] = AttributeValue(workspaceID)
        expressionAttributeValues[":itemType"] = AttributeValue("Relationship")
        expressionAttributeValues[":typeOfRelationship"] = AttributeValue("HIERARCHY")
        expressionAttributeValues[":itemStatus"] = AttributeValue(status.name)

        return DynamoDBQueryExpression<Relationship>()
            .withKeyConditionExpression("workspaceIdentifier = :workspaceIdentifier  and itemType = :itemType")
            .withIndexName("WS-itemType-index").withConsistentRead(false)
            .withFilterExpression("typeOfRelationship = :typeOfRelationship and itemStatus = :itemStatus")
            .withExpressionAttributeValues(expressionAttributeValues).let {
                mapper.query(Relationship::class.java, it, dynamoDBMapperConfig)
            }
    }

    fun getHierarchyRelationshipsWithStartNode(workspaceID: String, startNodeID: String, status: ItemStatus): List<Relationship> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":workspaceIdentifier"] = AttributeValue(workspaceID)
        expressionAttributeValues[":itemType"] = AttributeValue("Relationship")
        expressionAttributeValues[":typeOfRelationship"] = AttributeValue("HIERARCHY")
        expressionAttributeValues[":startNode"] = AttributeValue(startNodeID)
        expressionAttributeValues[":itemStatus"] = AttributeValue(status.name)

        // TODO(If number of relationships per node start increasing drastically, introduce a startNode-itemType GSI)
        return DynamoDBQueryExpression<Relationship>()
            .withKeyConditionExpression("workspaceIdentifier = :workspaceIdentifier  and itemType = :itemType")
            .withIndexName("WS-itemType-index").withConsistentRead(false)
            .withFilterExpression("typeOfRelationship = :typeOfRelationship and startNode = :startNode and itemStatus = :itemStatus ")
            .withExpressionAttributeValues(expressionAttributeValues).let {
                mapper.query(Relationship::class.java, it, dynamoDBMapperConfig)
            }
    }

    fun getHierarchyRelationshipsWithEndNode(workspaceID: String, endNodeID: String, status: ItemStatus): List<Relationship> {
        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":workspaceIdentifier"] = AttributeValue(workspaceID)
        expressionAttributeValues[":itemType"] = AttributeValue("Relationship")
        expressionAttributeValues[":typeOfRelationship"] = AttributeValue("HIERARCHY")
        expressionAttributeValues[":endNode"] = AttributeValue(endNodeID)
        expressionAttributeValues[":itemStatus"] = AttributeValue(status.name)

        // TODO(If number of relationships per node start increasing drastically, introduce a endNode-itemType GSI)
        return DynamoDBQueryExpression<Relationship>()
            .withKeyConditionExpression("workspaceIdentifier = :workspaceIdentifier  and itemType = :itemType")
            .withIndexName("WS-itemType-index").withConsistentRead(false)
            .withFilterExpression("typeOfRelationship = :typeOfRelationship and endNode = :endNode and itemStatus = :itemStatus")
            .withExpressionAttributeValues(expressionAttributeValues).let {
                mapper.query(Relationship::class.java, it, dynamoDBMapperConfig)
            }
    }

    fun changeRelationshipStatus(list: List<Relationship>, status: ItemStatus) {

        LOG.info(list)
        val table: Table = dynamoDB.getTable(tableName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":itemStatus"] = status.name

        for (relationship in list) {
            try {
                UpdateItemSpec().withPrimaryKey("PK", getRelationshipPK(relationship), "SK", getRelationshipSK(relationship))
                    .withUpdateExpression("SET itemStatus = :itemStatus")
                    .withValueMap(expressionAttributeValues)
                    .withConditionExpression("attribute_exists(PK)")
                    .also {
                        table.updateItem(it)
                    }
            } catch (e: ConditionalCheckFailedException) {
                LOG.warn("${relationship.id} not present in the DB")
            }
        }
    }

    fun getRelationship(startNode: String, endNode: String, workspaceID: String, relationshipType : RelationshipType) : Relationship?{

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":SK"] = AttributeValue("$startNode${Constants.DELIMITER}${relationshipType.name}")
        expressionAttributeValues[":PK"] = AttributeValue("RLSP")
        expressionAttributeValues[":typeOfRelationship"] = AttributeValue("HIERARCHY")
        expressionAttributeValues[":endNode"] = AttributeValue(endNode)
        expressionAttributeValues[":workspaceIdentifier"] = AttributeValue(workspaceID)

        DynamoDBQueryExpression<Relationship>()
                .withKeyConditionExpression("SK = :workspaceIdentifier  and begins_with(PK, :PK)")
                .withIndexName("SK-PK-index").withConsistentRead(false)
                .withFilterExpression("endNode = :endNode and workspaceIdentifier = :workspaceIdentifier")
                .withExpressionAttributeValues(expressionAttributeValues).let {
                    mapper.query(Relationship::class.java, it, dynamoDBMapperConfig).let { list ->
                        if(list.isNotEmpty()) return list[0]
                    }
                }

        return null

    }

    fun deleteRelationship(relationship: Relationship){
        mapper.delete(relationship)
    }



    private fun getRelationshipPK(relationship: Relationship): String {
        return relationship.id
    }

    private fun getRelationshipSK(relationship: Relationship): String {
        return relationship.sk
    }

    companion object {
        private val LOG = LogManager.getLogger(RelationshipRepository::class.java)
    }
}