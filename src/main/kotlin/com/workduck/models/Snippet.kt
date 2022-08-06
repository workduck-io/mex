package com.workduck.models


import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.convertersv2.ItemStatusConverterV2
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.NodeDataConverterV2
import com.workduck.convertersv2.SnippetIdentifierConverterV2
import com.workduck.convertersv2.WorkspaceIdentifierConverterV2
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

data class Snippet(

    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @get:DynamoDbConvertedBy(WorkspaceIdentifierConverterV2::class)
    @get:DynamoDbAttribute("PK")
    @get:DynamoDbPartitionKey
    override var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

    override var version: Int? = 1,

    var id: String = Helper.generateNanoID(IdentifierType.SNIPPET.name),

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Snippet,

    override var title: String = "New Snippet",

    @get:DynamoDbConvertedBy(ItemStatusConverterV2::class)
    var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @get:DynamoDbConvertedBy(NodeDataConverterV2::class)
    @get:DynamoDbAttribute("nodeData")
    override var data: List<AdvancedElement>? = null,


    override var dataOrder: MutableList<String>? = null,

    var template: Boolean? = null,

    override var createdBy: String? = null,

    override var lastEditedBy: String? = null,

    @get:DynamoDbConvertedBy(SnippetIdentifierConverterV2::class)
    var referenceSnippet : SnippetIdentifier? = null,

    override var publicAccess: Boolean = false,

    override var createdAt: Long? = Constants.getCurrentTime()

) : Entity, Page {

    override var updatedAt: Long = Constants.getCurrentTime()

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var sk: String = "$id${Constants.DELIMITER}$version"

    companion object {

        val SNIPPET_TABLE_SCHEMA: TableSchema<Snippet> = TableSchema.fromClass(Snippet::class.java)


        fun setCreatedFieldsNull(page: Page){
            page.createdAt = null
            page.createdBy = null
        }
    }
}
