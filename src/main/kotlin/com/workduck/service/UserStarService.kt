package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.serverless.models.requests.GenericListRequest
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.workduck.repositories.UserStarRepository
import com.workduck.utils.DDBHelper
import org.apache.logging.log4j.LogManager

class UserStarService(

    private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
    private val dynamoDB: DynamoDB = DynamoDB(client),
    private val mapper: DynamoDBMapper = DynamoDBMapper(client),

    private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

    val table: Table = dynamoDB.getTable(tableName),

    var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

    private val userStarRepository: UserStarRepository = UserStarRepository(mapper, dynamoDBMapperConfig, table)

) {

    fun createStar(userID: String, nodeID: String, workspaceID: String) {
        userStarRepository.createBookmark(userID, nodeID, workspaceID)
    }

    fun deleteStar(userID: String, nodeID: String, workspaceID: String) {
        userStarRepository.deleteStar(userID, nodeID, workspaceID)
    }

    fun getAllBookmarkedNodesByUser(userID: String, workspaceID: String): List<String> {
        return userStarRepository.getAllBookmarkedNodesByUser(userID, workspaceID)
    }

    fun isNodeStarredForUser(nodeID: String, userID: String, workspaceID: String): Boolean {
        return userStarRepository.isNodeStarredForUser(nodeID, userID, workspaceID)
    }

    fun createMultipleStars(userID: String, listRequest: WDRequest, workspaceID: String) {
        val nodeIDList = (listRequest as GenericListRequest).ids
        require(nodeIDList.all { nodeID ->
            nodeID.isValidID(Constants.NODE_ID_PREFIX)
        }) { "Invalid NodeIDs" }
        userStarRepository.createMultipleStars(userID, nodeIDList, workspaceID)
    }

    fun deleteMultipleStars(userID: String, listRequest: WDRequest, workspaceID: String) {
        val nodeIDList = (listRequest as GenericListRequest).ids
        require(nodeIDList.all { nodeID ->
            nodeID.isValidID(Constants.NODE_ID_PREFIX)
        }) { "Invalid NodeIDs" }
        userStarRepository.deleteMultipleStars(userID, nodeIDList, workspaceID)
    }

    companion object {
        private val LOG = LogManager.getLogger(UserStarService::class.java)
    }
}