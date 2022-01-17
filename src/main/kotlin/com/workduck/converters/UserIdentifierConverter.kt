package com.workduck.converters

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.workduck.models.UserIdentifier

class UserIdentifierConverter: DynamoDBTypeConverter<String, UserIdentifier?> {

    override fun convert(n: UserIdentifier?): String? = n?.id

    override fun unconvert(userID: String): UserIdentifier = UserIdentifier(userID)

}
