package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceIdentifier


class NamespaceIdentifierConverter : DynamoDBTypeConverter<String, NamespaceIdentifier?> {

	override fun convert(n: NamespaceIdentifier?): String? {
		return n?.id
	}

	override fun unconvert(nameSpaceID: String): NamespaceIdentifier {
		return NamespaceIdentifier(nameSpaceID)

	}

}