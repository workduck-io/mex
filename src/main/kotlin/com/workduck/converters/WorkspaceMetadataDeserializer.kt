package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.WorkspaceMetadata
import com.workduck.utils.Helper

class WorkspaceMetadataDeserializer : StdConverter<String, WorkspaceMetadata>() {
    override fun convert(workspaceMetadata: String?): WorkspaceMetadata? {
        return workspaceMetadata?.let { Helper.objectMapper.readValue(it) }
    }
}