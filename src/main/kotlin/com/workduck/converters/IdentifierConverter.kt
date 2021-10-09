package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.Identifier
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier


class IdentifierConverter : DynamoDBTypeConverter<String, Identifier?> {

	override fun convert(n: Identifier?): String? {
		return n?.id
	}

	override fun unconvert(id: String): Identifier {
		return if(id.startsWith("NAMESPACE")){
			NamespaceIdentifier(id)
		} else{
			WorkspaceIdentifier(id)
		}

	}

}