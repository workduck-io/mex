package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceIdentifier


class ItemTypeConverter : DynamoDBTypeConverter<String, String?> {

	override fun convert(itemType: String?): String? {
		return itemType
	}

	override fun unconvert(itemType: String): String {
		return itemType

	}

}