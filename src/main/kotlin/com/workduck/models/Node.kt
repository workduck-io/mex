package com.workduck.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.*
import com.workduck.convertersv2.ItemStatusConverterV2
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.NamespaceIdentifierConverterV2
import com.workduck.convertersv2.NodeDataConverterV2
import com.workduck.convertersv2.NodeMetaDataConverterV2
import com.workduck.convertersv2.NodeSchemaIdentifierConverterV2
import com.workduck.convertersv2.WorkspaceIdentifierConverterV2
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior


enum class NodeStatus {
    LINKED,
    UNLINKED
}

@DynamoDbBean
data class Node(

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var id: String = Helper.generateNanoID(IdentifierType.NODE.name),


    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @get:DynamoDbConvertedBy(WorkspaceIdentifierConverterV2::class)
    @get:DynamoDbAttribute("PK")
    @get:DynamoDbPartitionKey
    override var workspaceIdentifier: WorkspaceIdentifier = WorkspaceIdentifier("DefaultWorkspace"),

    override var title: String = "New Node",

    override var lastEditedBy: String? = null,

    @get:DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)
    override var createdBy: String? = null,


    @get:DynamoDbConvertedBy(NodeDataConverterV2::class)
    @get:DynamoDbAttribute("nodeData")
    override var data: List<AdvancedElement>? = null,

    @JsonProperty("metadata")
    @get:DynamoDbConvertedBy(NodeMetaDataConverterV2::class)
    @get:DynamoDbAttribute("metadata")
    var nodeMetaData : NodeMetadata ?= null,

    // TODO(write converter to store as map in DDB. And create Tag class)
    var tags: MutableList<String> = mutableListOf(),

    override var dataOrder: MutableList<String>? = null,

    @get:DynamoDbVersionAttribute
    override var version: Int? = null,


    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @get:DynamoDbConvertedBy(NamespaceIdentifierConverterV2::class)
    var namespaceIdentifier: NamespaceIdentifier? = null,

    /* WORKSPACE_ID#NAMESPACE_ID */
    @get:DynamoDbAttribute("AK")
    var ak: String = "${workspaceIdentifier.id}${Constants.DELIMITER}${namespaceIdentifier?.id}",


    @get:DynamoDbConvertedBy(NodeSchemaIdentifierConverterV2::class)
    var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,

    // @JsonProperty("status")
    // val status: NodeStatus = NodeStatus.LINKED,
    // val associatedProperties: Set<AssociatedProperty>,

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Node,


    @get:DynamoDbConvertedBy(ItemStatusConverterV2::class)
    override var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    var isBookmarked: Boolean? = null,

    @get:DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)
    override var publicAccess: Boolean = false,

    @get:DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)
    override var createdAt: Long? = Constants.getCurrentTime()

) : Entity, Page, ItemStatusAdherence {


    override var updatedAt: Long = Constants.getCurrentTime()

    var lastVersionCreatedAt: Long? = null


    var nodeVersionCount: Long = 0

    companion object {


        val NODE_TABLE_SCHEMA: TableSchema<Node> = TableSchema.fromClass(Node::class.java)

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
