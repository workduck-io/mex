package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.PageMetadata
import com.workduck.utils.Helper

class PageMetadataSerializer : StdConverter<PageMetadata, String>() {

    override fun convert(pageMetaData: PageMetadata?): String {
        return if(pageMetaData == null) return ""
        else Helper.objectMapper.writeValueAsString(pageMetaData)
    }


}