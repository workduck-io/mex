package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.PageMetadata
import com.workduck.utils.Helper

class PageMetadataDeserializer : StdConverter<String, PageMetadata>() {

    override fun convert(pageMetadataString: String?): PageMetadata? {
        return pageMetadataString?.let {  Helper.objectMapper.readValue(it) }
    }


}