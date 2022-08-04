package com.workduck.convertersv2

import com.workduck.models.ItemType
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class ItemTypeConverterV2 : AttributeConverter<ItemType> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(itemTypeEnum: ItemType): AttributeValue {
        return AttributeValue.builder().s(itemTypeEnum.name).build()
    }

    override fun transformTo(itemTypeString: AttributeValue): ItemType {
        return ItemType.valueOf(itemTypeString.s())
    }

    override fun type(): EnhancedType<ItemType> {
        return EnhancedType.of(ItemType::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}