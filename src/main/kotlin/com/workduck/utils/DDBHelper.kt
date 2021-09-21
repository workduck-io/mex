package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder

object DDBHelper {
    fun createDDBConnection(): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder
            .standard()
                //TODO: read from config file
            .withRegion(Regions.US_EAST_1)
            .build()
}