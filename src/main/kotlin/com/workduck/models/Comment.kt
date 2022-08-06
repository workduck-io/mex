package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants
import com.workduck.converters.CommentDataConverter
import com.workduck.converters.UserIdentifierConverter
import com.workduck.convertersv2.CommentDataConverterV2
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.UserIdentifierConverterV2
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@JsonIgnoreProperties(ignoreUnknown = true)
data class Comment(

    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var pk: String = "",

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var sk: String = "",

    @DynamoDBTypeConverted(converter = CommentDataConverter::class)
    @get:DynamoDbConvertedBy(CommentDataConverterV2::class)
    var commentBody: AdvancedElement ? = null,

    @DynamoDBTypeConverted(converter = UserIdentifierConverter::class)
    @get:DynamoDbConvertedBy(UserIdentifierConverterV2::class)
    @get:DynamoDbAttribute("AK")
    var commentedBy: UserIdentifier ? = null,

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Comment,

    var createdAt: Long = Constants.getCurrentTime()

) : Entity {

    companion object {
        val COMMENT_TABLE_SCHEMA: TableSchema<Comment> = TableSchema.fromClass(Comment::class.java)
    }

    var updatedAt: Long = Constants.getCurrentTime()
}
