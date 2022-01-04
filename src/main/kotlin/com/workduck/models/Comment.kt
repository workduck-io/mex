package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.fasterxml.jackson.annotation.JsonProperty

class Comment(

    @JsonProperty("pk")
    @DynamoDBHashKey(attributeName = "PK")
    var pk: String = "",

    @JsonProperty("sk")
    @DynamoDBRangeKey(attributeName = "SK")
    var sk: String = "",

    @JsonProperty("commentBody")
    @DynamoDBAttribute(attributeName = "commentBody")
    var commentBody: AdvancedElement ? = null,

    @JsonProperty("commentBy")
    @DynamoDBAttribute(attributeName = "commentBy")
    var commentedBy: String = "",

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = System.currentTimeMillis(),

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    override val itemType: String = "Comment"

) : Entity {
    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = System.currentTimeMillis()
}
