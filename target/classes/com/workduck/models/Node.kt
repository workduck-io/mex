package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.utils.Helper
import kotlin.streams.toList


enum class NodeStatus {
    LINKED,
    UNLINKED
}

@DynamoDBTable(tableName="elementsTable")
data class Node (

    @JsonProperty("id")
    @DynamoDBHashKey(attributeName="PK")
    var id: String = Helper.generateId("NODE"),

    @JsonProperty("data")
    @DynamoDBTypeConverted(converter = NodeDataConverter::class)
    @DynamoDBRangeKey(attributeName="SK")
    //@DynamoDBAttribute(attributeName="data")
    var data: List<Element> = listOf(),

    @JsonProperty("version")
    @DynamoDBAttribute(attributeName="version")
    var version: String? = null,

    @JsonProperty("namespaceIdentifier")
    @DynamoDBTypeConverted(converter = NameSpaceIdentifierConverter::class)
    @DynamoDBAttribute(attributeName="namespaceIdentifier")
    var namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("nodeSchemaIdentifier")
    @DynamoDBTypeConverted(converter = NodeSchemaIdentifierConverter::class)
    @DynamoDBAttribute(attributeName="nodeSchemaIdentifier")
    var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,

    //@JsonProperty("status")
    //val status: NodeStatus = NodeStatus.LINKED,
    //val associatedProperties: Set<AssociatedProperty>,

    @JsonProperty("createdAt")
    @DynamoDBAttribute(attributeName="createdAt")
    var createdAt: Long = System.currentTimeMillis()

): Entity{

    @JsonProperty("updatedAt")
    @DynamoDBAttribute(attributeName="updatedAt")
    var updatedAt : Long = System.currentTimeMillis()

   // override val partitionKey: String = "NODE#${id}"

   // override val sortKey: List<String> = listOf()//data.map{ element -> element.getID() }


}
