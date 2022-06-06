package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.HighlightMetadata
import com.workduck.utils.Helper

class HighlightMetadataConverter : DynamoDBTypeConverter<String, HighlightMetadata> {

    override fun convert(highlightMetadata: HighlightMetadata?): String {
        if (highlightMetadata == null) return ""
        return Helper.objectMapper.writeValueAsString(highlightMetadata)
    }

    override fun unconvert(highlightMetadata: String?): HighlightMetadata? {
        if(highlightMetadata.isNullOrEmpty()) return null
        return Helper.objectMapper.readValue(highlightMetadata)
    }
}