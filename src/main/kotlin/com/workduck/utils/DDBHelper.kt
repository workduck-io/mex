package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.Identifier


object DDBHelper {

	fun createDDBConnection(): AmazonDynamoDB = AmazonDynamoDBClientBuilder
		.standard()
		//TODO: read from config file
		.withRegion(Regions.US_EAST_1)
		.build()


	/*
	** Currently works for : NamespaceID and WorkspaceID
	*/
	fun getAllEntitiesWithIdentifierIDAndPrefix(
		akValue: String,
		indexName: String,
		dynamoDB: DynamoDB,
		itemType: String
	): MutableList<String> {

		val querySpec = QuerySpec()
		val objectMapper = ObjectMapper()

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
		)
			.withValueMap(expressionAttributeValues)


		val items: ItemCollection<QueryOutcome?>? = index.query(querySpec)
		val iterator: Iterator<Item> = items!!.iterator()

		val listOfJSON: MutableList<String> = mutableListOf()
		while (iterator.hasNext()) {
			val item: Item = iterator.next()
			listOfJSON += item.toJSON()
			//println(item.toJSONPretty())
		}

		return listOfJSON


	}
}