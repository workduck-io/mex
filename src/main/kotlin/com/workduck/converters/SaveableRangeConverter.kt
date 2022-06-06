package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.SaveableRange
import com.workduck.utils.Helper

class SaveableRangeConverter: DynamoDBTypeConverter<String, SaveableRange> {

    override fun convert(saveableRange: SaveableRange): String {
        return Helper.objectMapper.writeValueAsString(saveableRange)
    }

    override fun unconvert(saveableRange: String): SaveableRange {
        return Helper.objectMapper.readValue(saveableRange)
    }
}