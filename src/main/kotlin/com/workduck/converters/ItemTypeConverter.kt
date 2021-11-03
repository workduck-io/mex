package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter

class ItemTypeConverter : DynamoDBTypeConverter<String, String?> {

    override fun convert(itemType: String?): String? {
        return itemType
    }

    override fun unconvert(itemType: String): String {
        return itemType
    }
}
