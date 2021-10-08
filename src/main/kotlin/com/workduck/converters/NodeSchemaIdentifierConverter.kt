package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NodeSchemaIdentifier


class NodeSchemaIdentifierConverter : DynamoDBTypeConverter<String, NodeSchemaIdentifier?> {

	private val objectMapper = ObjectMapper()

	override fun convert(n: NodeSchemaIdentifier?): String? {
		return objectMapper.writeValueAsString(n)
	}

	override fun unconvert(nodeSchemaIdentifierString: String): NodeSchemaIdentifier {
		return objectMapper.readValue<NodeSchemaIdentifier>(nodeSchemaIdentifierString)

	}

}