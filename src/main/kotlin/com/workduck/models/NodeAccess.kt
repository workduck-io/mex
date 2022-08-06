package com.workduck.models


import com.fasterxml.jackson.annotation.JsonProperty
import com.serverless.utils.Constants
import com.workduck.convertersv2.AccessTypeConverterV2
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.NodeIdentifierConverterV2
import com.workduck.convertersv2.WorkspaceIdentifierConverterV2
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

enum class AccessType {
    WRITE,
    READ,
    MANAGE
}

class NodeAccess(

    @JsonProperty("nodeID")
    @get:DynamoDbConvertedBy(NodeIdentifierConverterV2::class)
    @get:DynamoDbAttribute("nodeID")
    var node: NodeIdentifier = NodeIdentifier("node"),

    @JsonProperty("workspaceID")
    @get:DynamoDbConvertedBy(WorkspaceIdentifierConverterV2::class)
    @get:DynamoDbAttribute("workspaceID")
    var workspace: WorkspaceIdentifier = WorkspaceIdentifier("workspace"),

    @get:DynamoDbAttribute("PK")
    @get:DynamoDbPartitionKey
    var pk: String = "${IdentifierType.NODE_ACCESS.name}${Constants.DELIMITER}${node.id}",


    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var userID: String = "user",

    var granterID: String = "granter",

    var ownerID: String = "owner",

    @get:DynamoDbConvertedBy(AccessTypeConverterV2::class)
    var accessType: AccessType = AccessType.WRITE,

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    var itemType: ItemType = ItemType.NodeAccess,

    var createdAt: Long? = Constants.getCurrentTime()

) {

    companion object {
        val NODE_ACCESS_TABLE_SCHEMA: TableSchema<NodeAccess> = TableSchema.fromClass(NodeAccess::class.java)
    }

    var updatedAt: Long = Constants.getCurrentTime()
}
