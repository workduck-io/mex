package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.*
import com.workduck.utils.Helper

@DynamoDBTable(tableName = "local-mex")
data class SmartCapture(

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBHashKey(attributeName = "PK")
    override var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

    @JsonProperty("version")
    @DynamoDBAttribute(attributeName = "version")
    override var version: Int? = 1,

    @JsonProperty("id")
    var id: String = Helper.generateNanoID(IdentifierType.SMART_CAPTURE.name),

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.SmartCapture,

    @JsonProperty("title")
    @DynamoDBAttribute(attributeName = "title")
    override var title: String = "New Capture",

    @JsonProperty("itemStatus")
    @DynamoDBAttribute(attributeName = "itemStatus")
    @DynamoDBTypeConverted(converter = ItemStatusConverter::class)
    var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = CaptureDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    override var data: List<BlockElement>? = null,

    @DynamoDBAttribute(attributeName = "dataOrder")
    override var dataOrder: MutableList<String>? = null,

    @DynamoDBAttribute(attributeName = "template")
    var template: Boolean? = null,

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    override var createdBy: String? = null,

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    override var lastEditedBy: String? = null,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    override var createdAt: Long? = Constants.getCurrentTime(),

    @JsonProperty("publicAccess")
    @DynamoDBAttribute(attributeName = "publicAccess")
    override var publicAccess: Boolean = false,

    @JsonProperty("metadata")
    @JsonSerialize(converter = PageMetadataSerializer::class)
    @JsonDeserialize(converter = PageMetadataDeserializer::class)
    @DynamoDBTypeConverted(converter = PageMetadataConverter::class)
    @DynamoDBAttribute(attributeName = "metadata")
    override var metadata: PageMetadata?= null,

) : Entity, Page<BlockElement> {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    override var updatedAt: Long = Constants.getCurrentTime()

    @JsonProperty("sk")
    @DynamoDBRangeKey(attributeName = "SK")
    var sk: String = "$id${Constants.DELIMITER}$version"
    companion object {
        fun setCreatedFieldsNull(page: Page<BlockElement>){
            page.createdAt = null
            page.createdBy = null
        }
    }
}

