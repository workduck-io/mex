package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.serverless.utils.Constants
import com.workduck.converters.ItemStatusConverter
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.NodeIdentifierConverter
import com.workduck.converters.RelationshipTypeConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.utils.Helper

enum class RelationshipType {
    CONTAINED, /* when node size exceeds threshold */
    HIERARCHY, /* parent- child hierarchy */
    LINKED /* iLinks */
}

@DynamoDBTable(tableName = "local-mex")
data class Relationship(

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName = "PK")
    var id: String = Helper.generateNanoID("RLSP"),

    @JsonProperty("sourceNode")
    @DynamoDBAttribute(attributeName = "AK")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var sourceNode: NodeIdentifier? = null,

    @JsonProperty("itemType")
    @DynamoDBAttribute(attributeName = "itemType")
    @DynamoDBTypeConverted(converter = ItemTypeConverter::class)
    override var itemType: ItemType = ItemType.Relationship,

    @JsonProperty("startNode")
    @DynamoDBAttribute(attributeName = "startNode")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var startNode: NodeIdentifier = NodeIdentifier(""),

    @JsonProperty("endNode")
    @DynamoDBAttribute(attributeName = "endNode")
    @DynamoDBTypeConverted(converter = NodeIdentifierConverter::class)
    var endNode: NodeIdentifier = NodeIdentifier(""),

    @JsonProperty("itemStatus")
    @DynamoDBAttribute(attributeName = "itemStatus")
    @DynamoDBTypeConverted(converter = ItemStatusConverter::class)
    override var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @JsonProperty("typeOfRelationship")
    @DynamoDBAttribute(attributeName = "typeOfRelationship")
    @DynamoDBTypeConverted(converter = RelationshipTypeConverter::class)
    var typeOfRelationship: RelationshipType = RelationshipType.HIERARCHY,

    @DynamoDBRangeKey(attributeName = "SK")
    var sk: String = "${startNode.id}${Constants.DELIMITER}${typeOfRelationship.name}",

    @JsonProperty("workspaceIdentifier")
    @DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName = "workspaceIdentifier")
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    @JsonProperty("authorizations")
    @DynamoDBAttribute(attributeName = "authorizations")
    var authorizations: Set<Auth>? = null,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName = "createdAt")
    var createdAt: Long = Constants.getCurrentTime()

) : Entity, ItemStatusAdherence {

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName = "updatedAt")
    var updatedAt = Constants.getCurrentTime()

//    init {
//        Preconditions.checkArgument(startNode.id != endNode.id)
//    }
}
