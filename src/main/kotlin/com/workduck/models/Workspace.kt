package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.serverless.utils.Constants
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.WorkspaceMetadataConverter
import com.workduck.converters.WorkspaceMetadataDeserializer
import com.workduck.utils.Helper

@DynamoDBTable(tableName = "sampleData")
@JsonIgnoreProperties(ignoreUnknown = true)
class Workspace(

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateId(IdentifierType.WORKSPACE.name),

    /* For convenient deletion */
    @JsonProperty("idCopy")
    @DynamoDBRangeKey(attributeName = "SK")
    var idCopy: String = id,

    @JsonProperty("name")
    @DynamoDBAttribute(attributeName = "workspaceName")
    var name: String = "DEFAULT_WORKSPACE",

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = Constants.getCurrentTime(),

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Workspace,


    @JsonProperty("metadata")
    @DynamoDBTypeConverted(converter = WorkspaceMetadataConverter::class)
    @JsonDeserialize(converter = WorkspaceMetadataDeserializer::class)
    @DynamoDBAttribute(attributeName = "metadata")
    var workspaceMetadata : WorkspaceMetadata ?= null,

) : Entity {


    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = Constants.getCurrentTime()
}
