package com.workduck.convertersv2

import com.workduck.models.SnippetIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class SnippetIdentifierConverterV2 : AttributeConverter<SnippetIdentifier> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(snippetIdentifier: SnippetIdentifier): AttributeValue {
        return AttributeValue.builder().s(snippetIdentifier.id).build()
    }

    override fun transformTo(snippetID: AttributeValue): SnippetIdentifier {
        return SnippetIdentifier(snippetID.s())
    }

    override fun type(): EnhancedType<SnippetIdentifier> {
        return EnhancedType.of(SnippetIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}