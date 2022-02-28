package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.RelationshipType

class RelationshipTypeConverter : DynamoDBTypeConverter<String, RelationshipType> {

    override fun convert(type: RelationshipType): String {
        return type.name
    }

    override fun unconvert(type: String): RelationshipType {
        return RelationshipType.valueOf(type)
    }
}
