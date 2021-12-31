package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.sqsNodeEventHandlers.NodeImage
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
    @DynamoDBAttribute(attributeName = "data")
    var data: List<AdvancedElement>? = null,

    // TODO(write converter to store as map in DDB. And create Tag class)
    @JsonProperty("tags")
    @DynamoDBAttribute(attributeName = "tags")
    var tags: MutableList<String>? = null,

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
        fun populateNodeWithSkAkAndCreatedAtNull(node: Node, storedNode: Node) {
            node.idCopy = node.id
            node.createdAt = storedNode.createdAt
            node.createdBy = storedNode.createdBy
            node.ak = node.workspaceIdentifier?.let { "${node.workspaceIdentifier?.id}#${node.namespaceIdentifier?.id}" }
        }

        // since we need to use different deserializer for data, I don't think we can avoid NodeImage.
        fun convertImageToNode(image: Map<String, Any>?): Node {
            val objectMapper = Helper.objectMapper
            val node = Node(
                id = image?.get("PK") as String,
                idCopy = image["SK"] as String,
                lastEditedBy = image["lastEditedBy"] as String,
                createdBy = image["createdBy"] as String,
                createdAt = image["createdAt"] as Long,
                itemStatus = image["itemStatus"] as String,
                workspaceIdentifier = WorkspaceIdentifier(image["workspaceIdentifier"] as String),
                namespaceIdentifier = NamespaceIdentifier(image["namespaceIdentifier"] as String),
                tags = image["tags"] as MutableList<String>?,
                version = image["version"] as Long?,
                data = objectMapper.readValue(objectMapper.writeValueAsString(image["data"])),
                dataOrder = image["dataOrder"] as MutableList<String>?,
                publicAccess = image["publicAccess"] as Boolean,

            )

            node.updatedAt = image["updatedAt"] as Long
            node.lastVersionCreatedAt = image["lastVersionCreatedAt"] as Long
            node.nodeVersionCount = image["nodeVersionCount"] as Long

            return node
        }

        fun convertNodeImageToNode(nodeImage: NodeImage): Node {
            val node = Node(
                id = nodeImage.id,
                idCopy = nodeImage.idCopy,
                lastEditedBy = nodeImage.lastEditedBy,
                createdBy = nodeImage.createdBy,
                createdAt = nodeImage.createdAt,
                itemStatus = nodeImage.itemStatus,
                workspaceIdentifier = nodeImage.workspaceIdentifier,
                namespaceIdentifier = nodeImage.namespaceIdentifier,
                tags = nodeImage.tags,
                version = nodeImage.version,
                data = nodeImage.data,
                dataOrder = nodeImage.dataOrder,
                publicAccess = nodeImage.publicAccess
            )

            node.updatedAt = nodeImage.updatedAt
            node.lastVersionCreatedAt = nodeImage.lastVersionCreatedAt
            node.nodeVersionCount = nodeImage.nodeVersionCount
            return node
        }
    }
}
