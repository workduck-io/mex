package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceMetadata
import com.workduck.utils.Helper

class NamespaceMetadataDeserializer : StdConverter<String, NamespaceMetadata>() {
    override fun convert(namespaceMetadata: String?): NamespaceMetadata? {
        return namespaceMetadata?.let { Helper.objectMapper.readValue(it) }
    }
}