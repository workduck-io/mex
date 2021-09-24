package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue



class NodeDataConverter : DynamoDBTypeConverter<String, List<Element>> {

	private val objectMapper = ObjectMapper()

	override fun convert(n : List<Element>): String? {
		return objectMapper.writeValueAsString(n)
	}

	override fun unconvert(nodeSchemaIdentifierString: String): List<Element> {
		return objectMapper.readValue<List<Element>>(nodeSchemaIdentifierString)

	}

}