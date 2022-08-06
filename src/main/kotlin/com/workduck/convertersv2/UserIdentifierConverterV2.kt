package com.workduck.convertersv2

import com.workduck.models.UserIdentifier
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class UserIdentifierConverterV2 : AttributeConverter<UserIdentifier> {

    val objectMapper = Helper.objectMapper
    override fun transformFrom(userIdentifier: UserIdentifier): AttributeValue {
        return AttributeValue.builder().s(userIdentifier.id).build()
    }

    override fun transformTo(userID: AttributeValue): UserIdentifier {
        return UserIdentifier(userID.s())
    }

    override fun type(): EnhancedType<UserIdentifier> {
        return EnhancedType.of(UserIdentifier::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}