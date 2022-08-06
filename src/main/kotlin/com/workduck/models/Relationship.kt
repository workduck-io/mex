package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.serverless.utils.Constants
import com.workduck.convertersv2.ItemStatusConverterV2
import com.workduck.convertersv2.ItemTypeConverterV2
import com.workduck.convertersv2.NodeIdentifierConverterV2
import com.workduck.convertersv2.RelationshipTypeConverterV2
import com.workduck.convertersv2.WorkspaceIdentifierConverterV2
import com.workduck.utils.Helper
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

enum class RelationshipType {
    CONTAINED, /* when node size exceeds threshold */
    HIERARCHY, /* parent- child hierarchy */
    LINKED /* iLinks */
}

@DynamoDBTable(tableName = "local-mex")
data class Relationship(

    @get:DynamoDbAttribute("PK")
    @get:DynamoDbPartitionKey
    var id: String = Helper.generateId("RLSP"),

    @get:DynamoDbConvertedBy(NodeIdentifierConverterV2::class)
    var sourceNode: NodeIdentifier? = null,

    @get:DynamoDbConvertedBy(ItemTypeConverterV2::class)
    override var itemType: ItemType = ItemType.Relationship,

    @get:DynamoDbConvertedBy(NodeIdentifierConverterV2::class)
    var startNode: NodeIdentifier = NodeIdentifier(""),

    @get:DynamoDbConvertedBy(NodeIdentifierConverterV2::class)
    var endNode: NodeIdentifier = NodeIdentifier(""),

    @get:DynamoDbConvertedBy(ItemStatusConverterV2::class)
    override var itemStatus: ItemStatus = ItemStatus.ACTIVE,

    @get:DynamoDbConvertedBy(RelationshipTypeConverterV2::class)
    var typeOfRelationship: RelationshipType = RelationshipType.HIERARCHY,

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var sk: String = "${startNode.id}${Constants.DELIMITER}${typeOfRelationship.name}",

    @get:DynamoDbConvertedBy(WorkspaceIdentifierConverterV2::class)
    var workspaceIdentifier: WorkspaceIdentifier? = null,

    var authorizations: Set<Auth>? = null,

    var createdAt: Long = Constants.getCurrentTime()

) : Entity, ItemStatusAdherence {

    var updatedAt = Constants.getCurrentTime()

    companion object {
        val RELATIONSHIP_TABLE_SCHEMA: TableSchema<Relationship> = TableSchema.fromClass(Relationship::class.java)
    }

//    init {
//        Preconditions.checkArgument(startNode.id != endNode.id)
//    }
}
