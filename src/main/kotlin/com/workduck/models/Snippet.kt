package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.ItemStatusConverter
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.NodeDataConverter
import com.workduck.converters.SnippetIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper

data class Snippet(

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBHashKey(attributeName = "PK")
    override var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

    @JsonProperty("version")
    @DynamoDBAttribute(attributeName = "version")
    override var version: Long? = 1,

    @JsonProperty("id")
    var id: String = Helper.generateNanoID(IdentifierType.SNIPPET.name),

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Snippet,

    @JsonProperty("title")
    @DynamoDBAttribute(attributeName = "title")
    override var title: String = "New Snippet",

    @JsonProperty("itemStatus")
    @DynamoDBAttribute(attributeName = "itemStatus")
    @DynamoDBTypeConverted(converter = ItemStatusConverter::class)
    var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    override var data: List<AdvancedElement>? = null,

    @DynamoDBAttribute(attributeName = "dataOrder")
    override var dataOrder: MutableList<String>? = null,

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    override var createdBy: String? = null,

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    override var lastEditedBy: String? = null,

    @JsonProperty("referenceSnippet")
    @DynamoDBAttribute(attributeName = "referenceSnippet")
    @DynamoDBTypeConverted(converter = SnippetIdentifierConverter::class)
    var referenceSnippet : SnippetIdentifier? = null,

//    @JsonProperty("namespaceIdentifier")
//    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
//    @JsonSerialize(converter = IdentifierSerializer::class)
//    @DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
//    @DynamoDBAttribute(attributeName = "namespaceIdentifier")
//    override var namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("publicAccess")
    @DynamoDBAttribute(attributeName = "publicAccess")
    override var publicAccess: Boolean = false,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    override var createdAt: Long? = System.currentTimeMillis()

) : Entity, Page {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    override var updatedAt: Long = System.currentTimeMillis()

    @JsonProperty("sk")
    @DynamoDBRangeKey(attributeName = "SK")
    var sk: String = "$id${Constants.DELIMITER}$version"

    companion object {
        fun setCreatedFieldsNull(page: Page){
            page.createdAt = null
            page.createdBy = null
        }
    }
}