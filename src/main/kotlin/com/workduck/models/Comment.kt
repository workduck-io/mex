package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.serverless.utils.Constants
import com.workduck.converters.CommentDataConverter
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.UserIdentifierConverter

@DynamoDBTable(tableName = "local-mex")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Comment(

    @JsonProperty("pk")
    @DynamoDBHashKey(attributeName = "PK")
    var pk: String = "",

    @JsonProperty("sk")
    @DynamoDBRangeKey(attributeName = "SK")
    var sk: String = "",

    @JsonProperty("commentBody")
    @DynamoDBTypeConverted(converter = CommentDataConverter::class)
    @DynamoDBAttribute(attributeName = "commentBody")
    var commentBody: AdvancedElement ? = null,

    @JsonProperty("commentedBy")
    @DynamoDBTypeConverted(converter = UserIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "AK")
    var commentedBy: UserIdentifier ? = null,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Comment,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long = Constants.getCurrentTime()

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = Constants.getCurrentTime()
}
