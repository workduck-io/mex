package com.workduck.convertersv2

import com.workduck.models.RelationshipType
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class RelationshipTypeConverterV2 : AttributeConverter<RelationshipType> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(relationshipTypeEnum: RelationshipType): AttributeValue {
        return AttributeValue.builder().s(relationshipTypeEnum.name).build()
    }

    override fun transformTo(relationshipTypeString: AttributeValue): RelationshipType {
        return RelationshipType.valueOf(relationshipTypeString.s())
    }

    override fun type(): EnhancedType<RelationshipType> {
        return EnhancedType.of(RelationshipType::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}