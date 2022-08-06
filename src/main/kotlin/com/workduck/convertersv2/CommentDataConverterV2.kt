package com.workduck.convertersv2

import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class CommentDataConverterV2 : AttributeConverter<AdvancedElement> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(advancedElementObject: AdvancedElement): AttributeValue {
        return AttributeValue.builder().s(objectMapper.writeValueAsString(advancedElementObject)).build()
    }

    override fun transformTo(advancedElementString: AttributeValue): AdvancedElement {
        return objectMapper.readValue(advancedElementString.s())
    }

    override fun type(): EnhancedType<AdvancedElement> {
        return EnhancedType.of(AdvancedElement::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}