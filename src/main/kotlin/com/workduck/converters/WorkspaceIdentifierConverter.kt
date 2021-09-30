package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.WorkspaceIdentifier

class WorkspaceIdentifierConverter : DynamoDBTypeConverter<String, WorkspaceIdentifier?> {

	private val objectMapper = ObjectMapper()

	override fun convert(n: WorkspaceIdentifier?): String? {
		return objectMapper.writeValueAsString(n)
	}

	override fun unconvert(workspaceIdentifierString: String): WorkspaceIdentifier {
		return objectMapper.readValue<WorkspaceIdentifier>(workspaceIdentifierString)

	}

}