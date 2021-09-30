package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.converters.NodeDataConverter
import com.workduck.converters.NodeSchemaIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.utils.Helper
import kotlin.streams.toList


enum class NodeStatus {
	LINKED,
	UNLINKED
}

@DynamoDBTable(tableName = "sampleData")
data class Node(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName = "PK")
	var id: String = Helper.generateId(IdentifierType.NODE.name),


	@JsonProperty("idCopy")
	@DynamoDBHashKey(attributeName = "SK")
	var idCopy: String = id,


	@JsonProperty("data")
	@DynamoDBTypeConverted(converter = NodeDataConverter::class)
	@DynamoDBAttribute(attributeName = "nodeData")
	var data: List<Element> = mutableListOf(),

	@JsonProperty("version")
	@DynamoDBAttribute(attributeName = "version")
	var version: String? = null,

	@JsonProperty("namespaceIdentifier")
	@DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
	@DynamoDBAttribute(attributeName = "namespaceIdentifier")
	var namespaceIdentifier: NamespaceIdentifier? = null,

	@JsonProperty("workspaceIdentifier")
	@DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
	@DynamoDBAttribute(attributeName = "workspaceIdentifier")
	var workspaceIdentifier: WorkspaceIdentifier? = null,

	@JsonProperty("nodeSchemaIdentifier")
	@DynamoDBTypeConverted(converter = NodeSchemaIdentifierConverter::class)
	@DynamoDBAttribute(attributeName = "nodeSchemaIdentifier")
	var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,

	//@JsonProperty("status")
	//val status: NodeStatus = NodeStatus.LINKED,
	//val associatedProperties: Set<AssociatedProperty>,

	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName = "createdAt")
	var createdAt: Long = System.currentTimeMillis()

) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName = "updatedAt")
	var updatedAt: Long = System.currentTimeMillis()

	//override val entityID: String = id

	//override val sortKey: List<String> = listOf()//data.map{ element -> element.getID() }

}
