package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.ElementTypes

class AdvancedElementPropertiesConverter: DynamoDBTypeConverter<String, ElementTypes> {

    override fun convert(elementType: ElementTypes): String {
        return elementType.getType()
    }

    override fun unconvert(accessType: String): ElementTypes {
        return ElementTypes.fromName(accessType) ?: throw IllegalArgumentException("Invalid Invalid Element Property $accessType")
    }
}

