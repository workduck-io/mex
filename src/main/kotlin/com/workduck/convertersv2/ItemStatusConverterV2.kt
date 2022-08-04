package com.workduck.convertersv2

import com.workduck.models.ItemStatus
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class ItemStatusConverterV2 : AttributeConverter<ItemStatus> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(itemStatusEnum: ItemStatus): AttributeValue {
        return AttributeValue.builder().s(itemStatusEnum.name).build()
    }

    override fun transformTo(itemStatusString: AttributeValue): ItemStatus {
        return ItemStatus.valueOf(itemStatusString.s())
    }

    override fun type(): EnhancedType<ItemStatus> {
        return EnhancedType.of(ItemStatus::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}