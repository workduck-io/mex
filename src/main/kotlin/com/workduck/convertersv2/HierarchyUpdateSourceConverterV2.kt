package com.workduck.convertersv2

import com.workduck.models.HierarchyUpdateSource
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class HierarchyUpdateSourceConverterV2 : AttributeConverter<HierarchyUpdateSource> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(hierarchyUpdateSourceEnum: HierarchyUpdateSource): AttributeValue {
        return AttributeValue.builder().s(hierarchyUpdateSourceEnum.name).build()
    }

    override fun transformTo(hierarchyUpdateSourceString: AttributeValue): HierarchyUpdateSource {
        return HierarchyUpdateSource.valueOf(hierarchyUpdateSourceString.s())
    }

    override fun type(): EnhancedType<HierarchyUpdateSource> {
        return EnhancedType.of(HierarchyUpdateSource::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}