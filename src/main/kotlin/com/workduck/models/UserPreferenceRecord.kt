package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.fasterxml.jackson.annotation.JsonProperty

@DynamoDBTable(tableName = "local-mex")
data class UserPreferenceRecord(

    @JsonProperty("userID")
    @DynamoDBHashKey(attributeName = "PK")
    var userID: String = "",

    @JsonProperty("preferenceType")
    @DynamoDBRangeKey(attributeName = "SK")
    var preferenceType: String = "",

    @JsonProperty("preferenceValue")
    @DynamoDBAttribute(attributeName = "userPreference")
    var preferenceValue: String = "",

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    var itemType: String = "UserPreferenceRecord",

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long = System.currentTimeMillis()
) {

}
