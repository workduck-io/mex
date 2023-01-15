package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceMetadata
import com.workduck.utils.Helper

class NamespaceMetadataDeserializer : StdConverter<String, NamespaceMetadata>() {
    override fun convert(nodeMetadata: String?): NamespaceMetadata? {
        return nodeMetadata?.let { Helper.objectMapper.readValue(it) }
    }
}