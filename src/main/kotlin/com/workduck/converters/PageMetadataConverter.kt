package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.PageMetadata
import com.workduck.utils.Helper

class PageMetadataConverter : DynamoDBTypeConverter<String, PageMetadata?> {
    override fun convert(pageMetadata: PageMetadata?): String? {
        return pageMetadata?.let {  Helper.objectMapper.writeValueAsString(it) }
    }

    override fun unconvert(pageMetadata: String?): PageMetadata? {
        return pageMetadata?.let {  Helper.objectMapper.readValue(it) }
    }


}