package com.workduck.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.workduck.repositories.TagRepository
import com.workduck.utils.DDBHelper

class TagService(
        private val client: AmazonDynamoDB = DDBHelper.createDDBConnection(),
        private val dynamoDB: DynamoDB = DynamoDB(client),
    // private val mapper = DynamoDBMapper(client)

        private val tableName: String = when (System.getenv("TABLE_NAME")) {
        null -> "local-mex" /* for local testing without serverless offline */
        else -> System.getenv("TABLE_NAME")
    },

        var dynamoDBMapperConfig: DynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build(),

        val table: Table = dynamoDB.getTable(tableName),

        private val tagRepository: TagRepository = TagRepository(dynamoDB, dynamoDBMapperConfig, client, tableName),
) {

    fun addTagForNode(tagName: String, nodeID: String, workspaceID: String) {
        tagRepository.addTagForNode(tagName, nodeID, workspaceID)
    }
}
