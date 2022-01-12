package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.NodeSchemaIdentifierConverter
import com.workduck.converters.NodeDataConverter
import com.workduck.deserializers.NodeObjectDataDeserializer
import com.workduck.utils.Helper

enum class NodeStatus {
    LINKED,
    UNLINKED
}

@DynamoDBTable(tableName = "local-mex")
data class Node(

    @JsonProperty("id")
    @JsonAlias("PK") /* to help convert DDB Image to Node */
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateId(IdentifierType.NODE.name),

    /* For convenient deletion */
    @JsonProperty("idCopy")
    @JsonAlias("SK") /* to help convert DDB Image to Node */
    @DynamoDBRangeKey(attributeName = "SK")
    var idCopy: String? = id,

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    var lastEditedBy: String? = null,

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    var createdBy: String? = null,

    @JsonProperty("data")
    @JsonAlias("nodeData")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    @JsonDeserialize(using = NodeObjectDataDeserializer::class)
    var data: Map<String, AdvancedElement>? = null,

    // TODO(write converter to store as map in DDB. And create Tag class)
    @JsonProperty("tags")
    @DynamoDBAttribute(attributeName = "tags")
    var tags: MutableList<String>? = null,

    @JsonProperty("dataOrder")
    @DynamoDBAttribute(attributeName = "dataOrder")
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
    @JsonProperty("AK")
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

    companion object {
        fun populateNodeWithSkAkAndCreatedAtNull(node: Node, storedNode: Node) {
            node.idCopy = node.id
            node.createdAt = storedNode.createdAt
            node.createdBy = storedNode.createdBy
            node.ak = node.workspaceIdentifier?.let { "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}" }
        }
    }
}
