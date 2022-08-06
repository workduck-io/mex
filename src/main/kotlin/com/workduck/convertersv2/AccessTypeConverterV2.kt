package com.workduck.convertersv2

import com.workduck.models.AccessType
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class AccessTypeConverterV2 : AttributeConverter<AccessType> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(accessTypeEnum: AccessType): AttributeValue {
        return AttributeValue.builder().s(accessTypeEnum.name).build()
    }

    override fun transformTo(accessTypeString: AttributeValue): AccessType {
        return AccessType.valueOf(accessTypeString.s())
    }

    override fun type(): EnhancedType<AccessType> {
        return EnhancedType.of(AccessType::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}