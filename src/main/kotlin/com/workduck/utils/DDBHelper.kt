package com.workduck.utils

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Index
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec

object DDBHelper {

//    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
//        AwsClientBuilder.EndpointConfiguration("http://host.docker.internal:8000", "us-east-1"))
//        .build()

    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder
        .standard()
        // TODO: read from config file
        .withRegion(Regions.US_EAST_1)
        .build()

}
