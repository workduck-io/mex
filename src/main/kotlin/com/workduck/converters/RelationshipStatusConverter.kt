package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.ItemStatus

class ItemStatusConverter : DynamoDBTypeConverter<String, ItemStatus> {

    override fun convert(status: ItemStatus): String {
        return status.name
    }

    override fun unconvert(status: String): ItemStatus {
        return ItemStatus.valueOf(status)
    }
}
