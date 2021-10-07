package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.WorkspaceIdentifier

class WorkspaceIdentifierConverter : DynamoDBTypeConverter<String, WorkspaceIdentifier?> {

	override fun convert(n: WorkspaceIdentifier?): String? {
		return n?.id
	}

	override fun unconvert(workspaceID: String): WorkspaceIdentifier {
		return WorkspaceIdentifier(workspaceID)

	}

}