package com.workduck.convertersv2

import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.NodeMetadata
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class NodeMetaDataConverterV2 : AttributeConverter<NodeMetadata?> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(nodeMetadataObject: NodeMetadata?): AttributeValue {
        return if(nodeMetadataObject == null) AttributeValue.builder().s("").build()
        else AttributeValue.builder().s(Helper.objectMapper.writeValueAsString(nodeMetadataObject)).build()
    }

    override fun transformTo(nodeMetadata: AttributeValue): NodeMetadata? {
        return Helper.objectMapper.readValue(nodeMetadata.s())
    }

    override fun type(): EnhancedType<NodeMetadata?> {
        return EnhancedType.of(NodeMetadata::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}