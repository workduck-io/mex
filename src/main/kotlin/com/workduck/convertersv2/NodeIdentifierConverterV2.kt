package com.workduck.convertersv2

import com.workduck.models.NodeIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class NodeIdentifierConverterV2 : AttributeConverter<NodeIdentifier> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(nodeIdentifier: NodeIdentifier): AttributeValue {
        return AttributeValue.builder().s(nodeIdentifier.id).build()
    }

    override fun transformTo(nodeID: AttributeValue): NodeIdentifier {
        return NodeIdentifier(nodeID.s())
    }

    override fun type(): EnhancedType<NodeIdentifier> {
        return EnhancedType.of(NodeIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}