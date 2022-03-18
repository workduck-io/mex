package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.ItemType

class ItemTypeConverter : DynamoDBTypeConverter<String, ItemType> {

    override fun convert(itemType: ItemType): String {
        return itemType.name
    }

    override fun unconvert(itemType: String): ItemType {
        return ItemType.valueOf(itemType)
    }
}
