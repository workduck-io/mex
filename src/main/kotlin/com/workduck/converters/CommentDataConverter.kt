package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.workduck.models.AdvancedElement
import com.workduck.utils.Helper

class CommentDataConverter : DynamoDBTypeConverter<String, AdvancedElement> {
    private val objectMapper = Helper.objectMapper

    override fun convert(n: AdvancedElement?): String? {
        return objectMapper.writeValueAsString(n)
    }

    override fun unconvert(n : String?): AdvancedElement? {
        return n?.let { objectMapper.readValue(it) }
    }


}