package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.RelationshipIdentifier

class RelationshipIdentifierConverter: DynamoDBTypeConverter<String, RelationshipIdentifier?> {

    override fun convert(n: RelationshipIdentifier?): String? =  n?.id


    override fun unconvert(relationshipID: String): RelationshipIdentifier = RelationshipIdentifier(relationshipID)

}