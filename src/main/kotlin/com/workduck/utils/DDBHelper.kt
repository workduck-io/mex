package com.workduck.utils

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder

object DDBHelper {

//    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
//        AwsClientBuilder.EndpointConfiguration("http://host.docker.internal:8000", "us-east-1"))
//        .build()

    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder
        .standard()
        // TODO: read from config file
        .withRegion(Regions.US_EAST_1)
        .build()

    fun getTableName() : String {
        return when (System.getenv("STAGE")) {
            null -> "local-mex" /* for local testing without serverless offline */
            "staging" -> "test-mex"
            else -> System.getenv("TABLE_NAME")
        }
    }

}
