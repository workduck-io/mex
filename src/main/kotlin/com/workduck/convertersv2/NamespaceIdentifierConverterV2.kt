package com.workduck.convertersv2

import com.workduck.models.NamespaceIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue


class NamespaceIdentifierConverterV2 : AttributeConverter<NamespaceIdentifier> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(namespaceIdentifierObject: NamespaceIdentifier): AttributeValue {
        return AttributeValue.builder().s(namespaceIdentifierObject.id).build()
    }

    override fun transformTo(namespaceID: AttributeValue): NamespaceIdentifier {
        return NamespaceIdentifier(namespaceID.s())
    }

    override fun type(): EnhancedType<NamespaceIdentifier> {
        return EnhancedType.of(NamespaceIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}