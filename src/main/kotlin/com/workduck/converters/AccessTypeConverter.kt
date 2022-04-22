package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.AccessType

class AccessTypeConverter : DynamoDBTypeConverter<String, AccessType> {

    override fun convert(accessType: AccessType): String {
        return accessType.name
    }

    override fun unconvert(accessType: String): AccessType {
        return AccessType.valueOf(accessType)
    }
}
