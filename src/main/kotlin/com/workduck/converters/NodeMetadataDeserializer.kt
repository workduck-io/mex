package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NodeMetadata
import com.workduck.utils.Helper

class NodeMetadataDeserializer : StdConverter<String, NodeMetadata>() {

    override fun convert(metadataString: String): NodeMetadata {
        return Helper.objectMapper.readValue(metadataString)
    }


}