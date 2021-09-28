package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue



class NamespaceIdentifierConverter : DynamoDBTypeConverter<String, NamespaceIdentifier?> {

	private val objectMapper = ObjectMapper()

	override fun convert(n : NamespaceIdentifier?): String? {
		return objectMapper.writeValueAsString(n)
	}

	override fun unconvert(nameSpaceIdentifierString: String): NamespaceIdentifier {
		return objectMapper.readValue<NamespaceIdentifier>(nameSpaceIdentifierString)

	}

}