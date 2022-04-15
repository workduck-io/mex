package com.workduck.repositories

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.document.DynamoDB

class TagRepository(
        private val dynamoDB: DynamoDB,
        var dynamoDBMapperConfig: DynamoDBMapperConfig,
        private val client: AmazonDynamoDB,
        var tableName: String

) {

    fun addTagForNode(tagName: String, nodeID: String, workspaceID: String){
        
    }
}