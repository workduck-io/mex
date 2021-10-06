package com.workduck.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.*
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.s3.model.JSONOutput
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.workduck.models.Identifier


object DDBHelper {
	fun createDDBConnection(): AmazonDynamoDB =
		AmazonDynamoDBClientBuilder
			.standard()
			//TODO: read from config file
			.withRegion(Regions.US_EAST_1)
			.build()


	/*
	** Currently works for Specifiers : Namespace Identifier and Workspace Identifier & Prefixes : NODE and USER
	*/
	fun getAllEntitiesWithIdentifierAndPrefix(
		identifier: Identifier, fieldName: String, indexName: String,
		prefix: String, dynamoDB: DynamoDB
	): MutableList<String> {

		val querySpec = QuerySpec()
		val objectMapper = ObjectMapper()
		val table: Table = dynamoDB.getTable("sampleData")
		val index: Index = table.getIndex(indexName)

		val expressionAttributeValues: MutableMap<String, Any> = HashMap()
		expressionAttributeValues[":identifier"] = objectMapper.writeValueAsString(identifier)
		expressionAttributeValues[":prefix"] = prefix

		querySpec.withKeyConditionExpression(
			"$fieldName = :identifier and begins_with(PK, :prefix)"
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