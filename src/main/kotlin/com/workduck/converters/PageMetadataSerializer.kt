package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.PageMetadata
import com.workduck.utils.Helper

class PageMetadataSerializer : StdConverter<PageMetadata, String>() {

    override fun convert(pageMetadata: PageMetadata?): String? {
        return pageMetadata?.let {  Helper.objectMapper.writeValueAsString(it) }
    }


}