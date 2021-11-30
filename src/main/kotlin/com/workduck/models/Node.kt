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

@DynamoDBTable(tableName = "local-mex")
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

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    var createdBy: String? = null,

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    var data: List<AdvancedElement>? = null,


    // TODO(write converter to store as map in DDB. And create Tag class)
    @JsonProperty("tags")
    @DynamoDBAttribute(attributeName = "tags")
    var tags: MutableList<String>? = null,

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


    @JsonProperty("itemStatus")
    @DynamoDBAttribute(attributeName = "itemStatus")
    var itemStatus: String = "ACTIVE",


    @JsonProperty("isBookmarked")
    @DynamoDBAttribute(attributeName = "isBookmarked")
    // TODO(make it part of NodeResponse object in code cleanup)
    var isBookmarked: Boolean? = null,

    @JsonProperty("publicAccess")
    @DynamoDBAttribute(attributeName = "publicAccess")
    var publicAccess: Boolean = false,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long? = System.currentTimeMillis()

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt: Long = System.currentTimeMillis()


    @JsonProperty("lastVersionCreatedAt")
    @DynamoDBAttribute(attributeName = "lastVersionCreatedAt")
    var lastVersionCreatedAt: Long? = null

    @JsonProperty("nodeVersionCount")
    @DynamoDBAttribute(attributeName = "nodeVersionCount")
    var nodeVersionCount: Long = 0

//    fun getVersion(): Long? {
//        return version
//    }
//
//    fun setVersion(version: Long?) {
//        this.version = version
//    }
    companion object {
        fun populateNodeWithSkAkAndCreatedAtNull(node : Node, storedNode : Node) {
            node.idCopy = node.id
            node.createdAt = storedNode.createdAt
            node.createdBy = storedNode.createdBy
            node.ak = node.workspaceIdentifier?.let{"${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}"}
        }
    }

}
