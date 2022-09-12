package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NodeMetadata
import com.workduck.utils.Helper

class NodeMetadataConverter : DynamoDBTypeConverter<String, NodeMetadata?> {
    override fun convert(nodeMetadata: NodeMetadata?): String {
        return if(nodeMetadata == null) return ""
        else Helper.objectMapper.writeValueAsString(nodeMetadata)
    }

    override fun unconvert(nodeMetadata: String): NodeMetadata? {
        return Helper.objectMapper.readValue(nodeMetadata)
    }


}