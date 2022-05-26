package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.serverless.utils.Constants
import com.workduck.converters.AccessTypeConverter
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.NodeIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierConverter

enum class AccessType {
    WRITE,
    READ,
    MANAGE
}

@DynamoDBTable(tableName = "local-mex")
class NodeAccess(

    @JsonProperty("nodeID")
    @DynamoDBAttribute(attributeName = "nodeID")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var node: NodeIdentifier = NodeIdentifier("node"),

    @JsonProperty("workspaceID")
    @DynamoDBAttribute(attributeName = "workspaceID")
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    var workspace: WorkspaceIdentifier = WorkspaceIdentifier("workspace"),

    @DynamoDBHashKey(attributeName = "PK")
    var pk: String = "${IdentifierType.NODE_ACCESS.name}${Constants.DELIMITER}${node.id}",

    @JsonProperty("userID")
    @DynamoDBRangeKey(attributeName = "SK")
    var userID: String = "user",

    @JsonProperty("granterID")
    @DynamoDBAttribute(attributeName = "granterID")
    var granterID: String = "granter",

    @JsonProperty("ownerID")
    @DynamoDBAttribute(attributeName = "ownerID")
    var ownerID: String = "owner",

    @JsonProperty("accessType")
    @DynamoDBAttribute(attributeName = "accessType")
    @DynamoDBTypeConverted(converter = AccessTypeConverter::class)
    var accessType: AccessType = AccessType.WRITE,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    var itemType: ItemType = ItemType.NodeAccess,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = Constants.getCurrentTime()

) {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = Constants.getCurrentTime()
}
