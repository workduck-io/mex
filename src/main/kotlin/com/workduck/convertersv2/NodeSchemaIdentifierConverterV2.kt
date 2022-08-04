package com.workduck.convertersv2

import com.workduck.models.NamespaceIdentifier
import com.workduck.models.NodeSchemaIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class NodeSchemaIdentifierConverterV2 : AttributeConverter<NodeSchemaIdentifier> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(nodeSchemaIdentifierObject: NodeSchemaIdentifier?): AttributeValue? {
        return AttributeValue.builder().s(nodeSchemaIdentifierObject?.id).build()
    }

    override fun transformTo(nodeSchemaID: AttributeValue): NodeSchemaIdentifier {
        return NodeSchemaIdentifier(nodeSchemaID.s())
    }

    override fun type(): EnhancedType<NodeSchemaIdentifier> {
        return EnhancedType.of(NodeSchemaIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}