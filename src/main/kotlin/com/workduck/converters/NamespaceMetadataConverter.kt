package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceMetadata
import com.workduck.utils.Helper

class NamespaceMetadataConverter : DynamoDBTypeConverter<String, NamespaceMetadata?> {
    override fun convert(namespaceMetadata: NamespaceMetadata?): String? {
        return namespaceMetadata?.let { Helper.objectMapper.writeValueAsString(it) }
    }

    override fun unconvert(namespaceMetadata: String?): NamespaceMetadata? {
        return namespaceMetadata?.let { Helper.objectMapper.readValue(it) }
    }


}