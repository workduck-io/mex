package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceMetadata
import com.workduck.models.WorkspaceMetadata
import com.workduck.utils.Helper

class WorkspaceMetadataConverter : DynamoDBTypeConverter<String, WorkspaceMetadata?> {
    override fun convert(workspaceMetadata: WorkspaceMetadata?): String? {
        return workspaceMetadata?.let { Helper.objectMapper.writeValueAsString(it) }
    }

    override fun unconvert(workspaceMetadata: String?): WorkspaceMetadata? {
        return workspaceMetadata?.let { Helper.objectMapper.readValue(it) }
    }


}