package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.serverless.utils.Constants
import com.workduck.convertersv2.ItemTypeConverterV2
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDBTable(tableName = "local-mex")
data class UserPreferenceRecord(

    @get:DynamoDbAttribute("PK")
    @get:DynamoDbPartitionKey
    var userID: String = "",

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var preferenceType: String = "",

    @get:DynamoDbAttribute("userPreference")
    var preferenceValue: String = "",

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    var itemType: ItemType = ItemType.UserPreferenceRecord,

    var createdAt: Long = Constants.getCurrentTime()
) {

    var updatedAt: Long = Constants.getCurrentTime()

    companion object {
        val USER_PREFERENCE_RECORD_TABLE_SCHEMA: TableSchema<UserPreferenceRecord> = TableSchema.fromClass(UserPreferenceRecord::class.java)
    }
}
