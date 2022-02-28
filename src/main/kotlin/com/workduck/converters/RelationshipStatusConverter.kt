package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.RelationshipStatus

class RelationshipStatusConverter : DynamoDBTypeConverter<String, RelationshipStatus> {

    override fun convert(status: RelationshipStatus): String {
        return status.name
    }

    override fun unconvert(status: String): RelationshipStatus {
        return RelationshipStatus.valueOf(status)
    }
}
