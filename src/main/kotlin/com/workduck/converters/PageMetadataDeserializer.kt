package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.PageMetadata
import com.workduck.utils.Helper

class PageMetadataDeserializer : StdConverter<String, PageMetadata>() {

    override fun convert(metadataString: String): PageMetadata {
        return Helper.objectMapper.readValue(metadataString)
    }


}