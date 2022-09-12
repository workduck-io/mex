package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Constants
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.ItemStatusConverter
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.NodeDataConverter
import com.workduck.converters.NodeMetadataConverter
import com.workduck.converters.NodeSchemaIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper

enum class NodeStatus {
    LINKED,
    UNLINKED
}

@DynamoDBTable(tableName = "local-mex")
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("metadata")
    @DynamoDBTypeConverted(converter = NodeMetadataConverter::class)
    @DynamoDBAttribute(attributeName = "metadata")
    var nodeMetaData : NodeMetadata ?= null,

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
    @DynamoDBAttribute(attributeName = "AK")
    var namespaceIdentifier: NamespaceIdentifier = NamespaceIdentifier("DefaultNamespace"),


    @JsonProperty("nodeSchemaIdentifier")
    @DynamoDBTypeConverted(converter = NodeSchemaIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "nodeSchemaIdentifier")
    var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,


    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Node,

    @JsonProperty("itemStatus")
    @DynamoDBAttribute(attributeName = "itemStatus")
    @DynamoDBTypeConverted(converter = ItemStatusConverter::class)
    override var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @JsonProperty("starred")
    @DynamoDBIgnore
    var starred: Boolean? = null, /* sent as null in node response if user didn't ask for this specifically */

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


    init {
        require(title.isNotBlank()) {
            "Node title needs to be provided by the user"
        }
    }
}
