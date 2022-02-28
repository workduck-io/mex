package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NodeIdentifierConverter
import com.workduck.converters.RelationshipStatusConverter
import com.workduck.converters.RelationshipTypeConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper

enum class RelationshipStatus(s: String) {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE")
}

enum class RelationshipType(s: String) {
    CONTAINED("CONTAINED"),
    LINKED("LINKED")
}

@DynamoDBTable(tableName = "local-mex")
data class Relationship(

    @JsonProperty("id")
    @DynamoDBAttribute(attributeName = "id")
    var id: String = Helper.generateId("RLSP"),

    @JsonProperty("sourceNode")
    @DynamoDBAttribute(attributeName = "sourceNode")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var sourceNode: NodeIdentifier = NodeIdentifier(""),

    @DynamoDBHashKey(attributeName = "PK")
    var pk: String = "${sourceNode.id}#RLSP",

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    override var itemType: String = "Relationship",

    @JsonProperty("startNode")
    @DynamoDBAttribute(attributeName = "startNode")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var startNode: NodeIdentifier = NodeIdentifier(""),


    @JsonProperty("endNode")
    @DynamoDBAttribute(attributeName = "endNode")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var endNode: NodeIdentifier = NodeIdentifier(""),


    @DynamoDBRangeKey(attributeName = "SK")
    var sk: String = "${startNode.id}#${endNode.id}",

    @JsonProperty("status")
    @DynamoDBAttribute(attributeName = "status")
    @DynamoDBTypeConverted(converter = RelationshipStatusConverter::class)
    var status: RelationshipStatus = RelationshipStatus.ACTIVE,

    @JsonProperty("type")
    @DynamoDBAttribute(attributeName = "type")
    @DynamoDBTypeConverted(converter = RelationshipTypeConverter::class)
    var type: RelationshipType? = null,

    @JsonProperty("workspaceIdentifier")
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "workspaceIdentifier")
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    @JsonProperty("authorizations")
    @DynamoDBAttribute(attributeName = "authorizations")
    var authorizations: Set<Auth>? = null,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long = System.currentTimeMillis()

) : Entity {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt = System.currentTimeMillis()

//    init {
//        Preconditions.checkArgument(startNode.id != endNode.id)
//    }
}
