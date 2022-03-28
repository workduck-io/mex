package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.serverless.utils.Constants
import com.workduck.converters.ItemTypeConverter
import com.workduck.utils.Helper

@DynamoDBTable(tableName = "sampleData")
class User(

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateId(IdentifierType.USER.name),

    @JsonProperty("uniqueID")
    @DynamoDBRangeKey(attributeName = "SK")
    var idCopy: String? = id,

    @JsonProperty("name")
    @DynamoDBAttribute(attributeName = "userName")
    var name: String? = null,

    @JsonProperty("email")
    @DynamoDBAttribute(attributeName = "userEmail")
    var email: String? = null,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.User,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = Constants.getCurrentTime()
) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = Constants.getCurrentTime()
}
