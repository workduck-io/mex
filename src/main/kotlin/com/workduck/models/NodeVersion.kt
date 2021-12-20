package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.*

@DynamoDBTable(tableName = "local-mex")
data class NodeVersion(

        @JsonProperty("id")
        @DynamoDBHashKey(attributeName = "PK")
        var id: String ?= null,

        /* For convenient deletion */
        @JsonProperty("updatedAt")
        @DynamoDBRangeKey(attributeName = "SK")
        var updatedAt: String? = null,

        @JsonProperty("lastEditedBy")
        @DynamoDBAttribute(attributeName = "lastEditedBy")
        var lastEditedBy: String? = null,

        @JsonProperty("createBy")
        @DynamoDBAttribute(attributeName = "createBy")
        var createdBy: String? = null,

        @JsonProperty("data")
        @DynamoDBTypeConverted(converter = NodeDataConverter::class)
        @DynamoDBAttribute(attributeName = "data")
        var data: List<AdvancedElement>? = null,

        @DynamoDBAttribute(attributeName = "dataOrder")
        var dataOrder: MutableList<String>? = null,

        @JsonProperty("version")
        @DynamoDBAttribute(attributeName = "version")
        var version: String? = null,

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
        override var itemType: String = "Node Version",

        @JsonProperty("createdAt")
        @DynamoDBAttribute(attributeName = "createdAt")
        var createdAt: Long? = System.currentTimeMillis(),


        @JsonProperty("versionStatus")
        @DynamoDBAttribute(attributeName = "versionStatus")
        var versionStatus: String = "ACTIVE"

) : Entity {

        @JsonProperty("timeToLive")
        @DynamoDBAttribute(attributeName = "timeToLive")
        var timeToLive: Long? = null

}
