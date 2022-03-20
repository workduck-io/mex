package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.NodeDataConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper

data class Snippet(

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateId(IdentifierType.SNIPPET.name),

    /* For convenient deletion */
    @JsonProperty("idCopy")
    @DynamoDBRangeKey(attributeName = "SK")
    var idCopy: String? = id,

    @DynamoDBAttribute(attributeName = "AK")
    override var ak: String? = null,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    override val itemType: String = "Snippet",

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBAttribute(attributeName = "nodeData")
    override var data: List<AdvancedElement>? = null,

    @DynamoDBAttribute(attributeName = "dataOrder")
    override var dataOrder: MutableList<String>? = null,

    @JsonProperty("createdBy")
    @DynamoDBAttribute(attributeName = "createdBy")
    override var createdBy: String? = null,

    @JsonProperty("lastEditedBy")
    @DynamoDBAttribute(attributeName = "lastEditedBy")
    override val lastEditedBy: String,

    @JsonProperty("workspaceIdentifier")
    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "workspaceIdentifier")
    override val workspaceIdentifier: WorkspaceIdentifier,

    @JsonProperty("namespaceIdentifier")
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    @JsonSerialize(converter = IdentifierSerializer::class)
    @DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "namespaceIdentifier")
    override val namespaceIdentifier: NamespaceIdentifier?,

    @JsonProperty("version")
    @DynamoDBVersionAttribute(attributeName = "version")
    override var version: Long? = null,

    @JsonProperty("publicAccess")
    @DynamoDBAttribute(attributeName = "publicAccess")
    override val publicAccess: Boolean = false,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    override var createdAt: Long = System.currentTimeMillis()

) : Entity, Page {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    override var updatedAt: Long = System.currentTimeMillis()
}
