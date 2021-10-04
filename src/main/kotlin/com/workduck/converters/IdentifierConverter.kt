package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Identifier
import com.workduck.models.NamespaceIdentifier


class IdentifierConverter : DynamoDBTypeConverter<String, Identifier?> {

	private val objectMapper = ObjectMapper()

	override fun convert(n: Identifier?): String? {
		return objectMapper.writeValueAsString(n)
	}

	override fun unconvert(IdentifierString: String): Identifier {
		return objectMapper.readValue<Identifier>(IdentifierString)

	}

}