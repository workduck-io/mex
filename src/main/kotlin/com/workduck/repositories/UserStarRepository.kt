package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.serverless.utils.Constants
import com.workduck.models.UserStar
import com.workduck.models.WorkspaceIdentifier
import org.apache.logging.log4j.LogManager

class UserStarRepository(
    private val mapper: DynamoDBMapper,
    private val dynamoDBMapperConfig: DynamoDBMapperConfig,
    private val table: Table,

) {

    fun createBookmark(userID: String, nodeID: String, workspaceID: String) {
        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()

            expressionAttributeValues[":nodeID"] = nodeID
            expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()

            val updateExpression = "SET starredNodes.$nodeID = :nodeID, updatedAt = :updatedAt"
            val conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"

            UpdateItemSpec().update(pk = workspaceID, sk = UserStar.getSK(userID), updateExpression = updateExpression,
                    expressionAttributeValues = expressionAttributeValues, conditionExpression = conditionExpression).let {
                table.updateItem(it)
            }

        } catch (e: ConditionalCheckFailedException) {
            val userStarRecord = UserStar(
                    workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                    userID = userID,
                    starredNodes = mapOf(nodeID to nodeID)
            )
            mapper.save(userStarRecord, dynamoDBMapperConfig)
        }
    }


    fun deleteStar(userID: String, nodeID: String, workspaceID: String) {

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()

        val updateExpression = "SET updatedAt = :updatedAt REMOVE starredNodes.$nodeID"
        val conditionExpression = "attribute_exists(PK) and attribute_exists(SK) and attribute_exists(starredNodes.$nodeID)"

        try {
            return UpdateItemSpec().update(
                    pk = workspaceID, sk = UserStar.getSK(userID), updateExpression = updateExpression,
                    expressionAttributeValues = expressionAttributeValues, conditionExpression = conditionExpression
            ).let {
                table.updateItem(it)
            }
        } catch (e : ConditionalCheckFailedException) {
            throw ConditionalCheckFailedException("Node to be Un-starred does not exist")
        }
    }


    fun isNodeStarredForUser(nodeID: String, userID: String, workspaceID: String): Boolean {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(UserStar.getSK(userID))

        return DynamoDBQueryExpression<UserStar>().query(
            keyConditionExpression = "PK = :PK and SK = :SK",
            filterExpression = "attribute_exists(starredNodes.$nodeID)", expressionAttributeValues = expressionAttributeValues,
            projectionExpression = "bookmarkedNodes.$nodeID"
        ).let {
            mapper.query(UserStar::class.java, it, dynamoDBMapperConfig).isNotEmpty()
        }
    }


    fun getAllBookmarkedNodesByUser(userID: String, workspaceID: String): List<String> {

        val expressionAttributeValues: MutableMap<String, AttributeValue> = HashMap()
        expressionAttributeValues[":PK"] = AttributeValue(workspaceID)
        expressionAttributeValues[":SK"] = AttributeValue(UserStar.getSK(userID))

        return DynamoDBQueryExpression<UserStar>().query(
                keyConditionExpression = "PK = :PK and SK = :SK", expressionAttributeValues = expressionAttributeValues
        ).let {
            mapper.query(UserStar::class.java, it, dynamoDBMapperConfig).firstOrNull()?.let {  record ->
                record.starredNodes.keys.toList()
            } ?: listOf()
        }
    }


    fun createMultipleStars(userID: String, nodeIDList: List<String>, workspaceID: String) {
        try {
            val expressionAttributeValues: MutableMap<String, Any> = HashMap()

            expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()

            var updateExpression = "SET updatedAt = :updatedAt, "
            for ((counter, nodeID) in nodeIDList.withIndex()) {
                updateExpression += " starredNodes.$nodeID = :val$counter,"
                expressionAttributeValues[":val$counter"] = nodeID
            }

            updateExpression = updateExpression.dropLast(1) /* drop the last comma */

            val conditionExpression = "attribute_exists(PK) and attribute_exists(SK)"

            UpdateItemSpec().update(pk = workspaceID, sk = UserStar.getSK(userID), updateExpression = updateExpression,
            expressionAttributeValues = expressionAttributeValues, conditionExpression = conditionExpression).let {
                table.updateItem(it)
            }

        } catch (e: ConditionalCheckFailedException) {

            val nodeIDMap = mutableMapOf<String, String>()
            for (nodeID in nodeIDList) {
                nodeIDMap[nodeID] = nodeID
            }

            val userStarRecord = UserStar(
                    workspaceIdentifier = WorkspaceIdentifier(workspaceID),
                    userID = userID,
                    starredNodes = nodeIDMap
            )

            mapper.save(userStarRecord, dynamoDBMapperConfig)
        }
    }

    fun deleteMultipleStars(userID: String, nodeIDList: List<String>, workspaceID: String) {
        val expressionAttributeValues: MutableMap<String, Any> = HashMap()

        expressionAttributeValues[":updatedAt"] = Constants.getCurrentTime()

        var updateExpression = "SET updatedAt = :updatedAt REMOVE"

        for (nodeID in nodeIDList) {
            updateExpression += " starredNodes.$nodeID,"
        }

        /* remove the extra comma */
        updateExpression = updateExpression.dropLast(1)

        UpdateItemSpec().update(pk = workspaceID, sk = UserStar.getSK(userID), updateExpression = updateExpression,
                expressionAttributeValues = expressionAttributeValues).let {
            table.updateItem(it)
        }


    }

    companion object {
        private val LOG = LogManager.getLogger(UserStarRepository::class.java)
    }
}
