package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NamespaceMetadata
import com.workduck.utils.Helper

class NamespaceMetadataConverter : DynamoDBTypeConverter<String, NamespaceMetadata?> {
    override fun convert(namespaceMetadata: NamespaceMetadata?): String {
        return if(namespaceMetadata == null) return ""
        else Helper.objectMapper.writeValueAsString(namespaceMetadata)
    }

    override fun unconvert(nodeMetadata: String): NamespaceMetadata? {
        return Helper.objectMapper.readValue(nodeMetadata)
    }


}