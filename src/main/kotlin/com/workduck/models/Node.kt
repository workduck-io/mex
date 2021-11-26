package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.*
import com.workduck.utils.Helper

enum class NodeStatus {
    LINKED,
    UNLINKED
}

@DynamoDBTable(tableName = "sampleData")
data class Node(

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateId(IdentifierType.NODE.name),

    /* For convenient deletion */
    @JsonProperty("idCopy")
    @DynamoDBRangeKey(attributeName = "SK")
    var idCopy: String? = id,

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    var lastEditedBy: String? = null,

    @JsonProperty("createBy")
    @DynamoDBAttribute(attributeName = "createBy")
    var createBy: String? = null,

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    var data: List<AdvancedElement>? = null,

    @DynamoDBAttribute(attributeName = "nodeDataOrder")
    var dataOrder: MutableList<String>? = null,

    @JsonProperty("version")
    @DynamoDBVersionAttribute(attributeName = "version")
    var version: Long? = null,

    @JsonProperty("namespaceIdentifier")
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "namespaceIdentifier")
    var namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "workspaceIdentifier")
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    /* WORKSPACE_ID#NAMESPACE_ID */
    @DynamoDBAttribute(attributeName = "AK")
    var ak: String? = null,

    @JsonProperty("nodeSchemaIdentifier")
    @DynamoDBTypeConverted(converter = NodeSchemaIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "nodeSchemaIdentifier")
    var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,

    // @JsonProperty("status")
    // val status: NodeStatus = NodeStatus.LINKED,
    // val associatedProperties: Set<AssociatedProperty>,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    override var itemType: String = "Node",


    @JsonProperty("isBookmarked")
    @DynamoDBAttribute(attributeName = "isBookmarked")
    // TODO(make it part of NodeResponse object in code cleanup)
    var isBookmarked: Boolean? = null,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = System.currentTimeMillis()

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = System.currentTimeMillis()

    companion object {
        fun populateNodeWithSkAkAndCreatedAtNull(node : Node) {
            node.idCopy = node.id
            node.createdAt = null
            node.ak = node.workspaceIdentifier?.let{"${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"}
        }
    }

}
