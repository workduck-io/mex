package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.CommentDataConverter

@DynamoDBTable(tableName = "local-mex")
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

    @JsonProperty("commentBy")
    @DynamoDBAttribute(attributeName = "commentBy")
    var commentedBy: String = "",


    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    override var itemType: String = "Comment",

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = System.currentTimeMillis()


) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = System.currentTimeMillis()
}
