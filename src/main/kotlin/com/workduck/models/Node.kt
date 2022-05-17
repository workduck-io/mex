package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.*
import com.workduck.utils.Helper

enum class NodeStatus {
    LINKED,
    UNLINKED
}

@DynamoDBTable(tableName = "local-mex")
data class Node(

    @JsonProperty("id")
    @DynamoDBRangeKey(attributeName = "SK")
    var id: String = Helper.generateNanoID(IdentifierType.NODE.name),

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBHashKey(attributeName = "PK")
    override var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

//    @JsonProperty("parentNodeID")
//    @DynamoDBAttribute(attributeName = "parentNodeID")
//    var parentNodeID: String = id,

    @JsonProperty("title")
    @DynamoDBAttribute(attributeName = "title")
    override var title: String = "New Node",

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    override var lastEditedBy: String? = null,

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    override var createdBy: String? = null,

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    override var data: List<AdvancedElement>? = null,

    // TODO(write converter to store as map in DDB. And create Tag class)
    @JsonProperty("tags")
    @DynamoDBAttribute(attributeName = "tags")
    var tags: MutableList<String> = mutableListOf(),

    @DynamoDBAttribute(attributeName = "dataOrder")
    override var dataOrder: MutableList<String>? = null,

    @JsonProperty("version")
    @DynamoDBVersionAttribute(attributeName = "version")
    override var version: Int? = null,

    @JsonProperty("namespaceIdentifier")
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "namespaceIdentifier") var namespaceIdentifier: NamespaceIdentifier? = null,

    /* WORKSPACE_ID#NAMESPACE_ID */
    @DynamoDBAttribute(attributeName = "AK")
    var ak: String = "${workspaceIdentifier.id}${Constants.DELIMITER}${namespaceIdentifier?.id}",

    @JsonProperty("nodeSchemaIdentifier")
    @DynamoDBTypeConverted(converter = NodeSchemaIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "nodeSchemaIdentifier")
    var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,
//
//    @JsonProperty("saveableRange")
//    @DynamoDBTypeConverted(converter = SaveableRangeConverter::class)
//    @DynamoDBAttribute(attributeName = "saveableRange")
//    var saveableRange: SaveableRange? = null,
//
//    @JsonProperty("sourceUrl")
//    @DynamoDBAttribute(attributeName = "sourceUrl")
//    var sourceUrl: String? = null,

    // @JsonProperty("status")
    // val status: NodeStatus = NodeStatus.LINKED,
    // val associatedProperties: Set<AssociatedProperty>,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Node,

    @JsonProperty("itemStatus")
    @DynamoDBAttribute(attributeName = "itemStatus")
    @DynamoDBTypeConverted(converter = ItemStatusConverter::class)
    override var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @JsonProperty("isBookmarked")
    @DynamoDBAttribute(attributeName = "isBookmarked")
// TODO(make it part of NodeResponse object in code cleanup)
    var isBookmarked: Boolean? = null,

    @JsonProperty("publicAccess")
    @DynamoDBAttribute(attributeName = "publicAccess")
    override var publicAccess: Boolean = false,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    override var createdAt: Long? = Constants.getCurrentTime()

) : Entity, Page, ItemStatusAdherence {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    override var updatedAt: Long = Constants.getCurrentTime()

    @JsonProperty("lastVersionCreatedAt")
    @DynamoDBAttribute(attributeName = "lastVersionCreatedAt")
    var lastVersionCreatedAt: Long? = null

    @JsonProperty("nodeVersionCount")
    @DynamoDBAttribute(attributeName = "nodeVersionCount")
    var nodeVersionCount: Long = 0

    companion object {
        fun populateNodeWithSkAkAndCreatedAt(node: Node, storedNode: Node) {
            node.createdAt = storedNode.createdAt
            node.createdBy = storedNode.createdBy
            node.ak = "${node.workspaceIdentifier.id}${Constants.DELIMITER}${node.namespaceIdentifier?.id}"
        }
    }

    init {
        require(title.isNotBlank()) {
            "Node title needs to be provided by the user"
        }
    }
}
