package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.document.Item
import com.fasterxml.jackson.annotation.JsonProperty
import com.serverless.utils.Constants
import com.workduck.converters.ItemTypeConverter

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
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    var itemType: ItemType = ItemType.UserPreferenceRecord,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long = Constants.getCurrentTime()
) {

}
