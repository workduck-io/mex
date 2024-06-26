package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.amazonaws.services.dynamodbv2.document.Item
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.*
import com.workduck.utils.Helper

@DynamoDBTable(tableName = "local-mex")
class UserIdentifierRecord(

    @JsonProperty("userID")
    @DynamoDBHashKey(attributeName = "PK")
    var userID: String = Helper.generateId("USER"),

    @JsonProperty("identifier")
    @DynamoDBRangeKey(attributeName = "SK")
    @JsonDeserialize(converter = IdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = IdentifierConverter::class)
    var identifier: Identifier? = null,

	/* AK = IdentifierID
	** Making this so that we don't have to create itemType-SK index. We can leverage already existing itemType-AK index
	*/
    @DynamoDBAttribute(attributeName = "AK")
    var ak: String? = identifier?.id,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = Constants.getCurrentTime(),

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.UserIdentifierRecord

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updateAt: Long = Constants.getCurrentTime()
}
