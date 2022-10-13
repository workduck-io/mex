package com.workduck.converters

import com.fasterxml.jackson.databind.util.StdConverter
import com.workduck.models.NodeMetadata
import com.workduck.utils.Helper

class NodeMetadataSerializer : StdConverter<NodeMetadata, String>() {

    override fun convert(nodeMetaData: NodeMetadata?): String {
        return if(nodeMetaData == null) return ""
        else Helper.objectMapper.writeValueAsString(nodeMetaData)
    }


}