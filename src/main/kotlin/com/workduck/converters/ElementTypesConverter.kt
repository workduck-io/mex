package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.ElementTypes

class ElementTypesConverter : DynamoDBTypeConverter<String, ElementTypes> {

    override fun convert(elementType: ElementTypes): String {
        return elementType.getType()
    }

    override fun unconvert(elementType: String): ElementTypes {
        return ElementTypes.fromName(elementType) ?: throw IllegalArgumentException("Invalid Element Type $elementType")
    }
}
