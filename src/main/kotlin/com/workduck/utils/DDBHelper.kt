package com.workduck.utils

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder

object DDBHelper {

    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
        AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
        .build()

//    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder
//        .standard()
//        // TODO: read from config file
//        .withRegion(Regions.US_EAST_1)
//        .build()

    fun getTableName() : String {
        return when(getStage()){
            "prod" -> "${getStage()}-mex-backend-ddb"
            else -> "${getStage()}-mex"
        }
    }

    fun getStage() : String {
        return when (System.getenv("STAGE")) {
            null -> "local" /* for local testing without serverless offline */
            else -> System.getenv("STAGE")
        }

    }

}
