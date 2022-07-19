package com.workduck.utils

import com.amazonaws.client.builder.AwsClientBuilder
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

    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
        AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
        .build()

//    fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder
//        .standard()
//        // TODO: read from config file
//        .withRegion(Regions.US_EAST_1)
//        .build()


	/*
	** Currently, works for : NamespaceID and WorkspaceID. Return List of Strings, not objects
	*/
    fun getAllEntitiesWithIdentifierIDAndPrefix(
        akValue: String,
        indexName: String,
        dynamoDB: DynamoDB,
        itemType: String
    ): MutableList<String> {

        val querySpec = QuerySpec()

        val tableName: String = when (System.getenv("TABLE_NAME")) {
            null -> "local-mex" /* for local testing without serverless offline */
            else -> System.getenv("TABLE_NAME")
        }

        val table: Table = dynamoDB.getTable(tableName)
        val index: Index = table.getIndex(indexName)

        val expressionAttributeValues: MutableMap<String, Any> = HashMap()
        expressionAttributeValues[":akValue"] = akValue
        expressionAttributeValues[":itemType"] = itemType

        querySpec.withKeyConditionExpression(
            "itemType = :itemType and begins_with(AK, :akValue)"
        ).withValueMap(expressionAttributeValues)
        .withProjectionExpression("PK")

        val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)
        val iterator: Iterator<Item> = items!!.iterator()

        var itemList: MutableList<String> = mutableListOf()
        while (iterator.hasNext()) {
            val item: Item = iterator.next()
            itemList = (itemList + (item["PK"] as String)).toMutableList()
        }
        return itemList
    }
}
