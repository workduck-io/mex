package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.*
import com.workduck.utils.Helper


enum class NodeStatus {
	LINKED,
	UNLINKED
}

@DynamoDBTable(tableName = "sampleData")
data class Node(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName = "PK")
	var id: String = Helper.generateId(IdentifierType.NODE.name),


	/* For convenient deletion */
	@JsonProperty("idCopy")
	@DynamoDBRangeKey(attributeName = "SK")
	var idCopy: String? = id,


	@JsonProperty("data")
	@DynamoDBTypeConverted(converter = NodeDataConverter::class)
	@DynamoDBAttribute(attributeName = "nodeData")
	var data: MutableList<AdvancedElement>? = null,

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
	var ak : String?= null,

	@JsonProperty("nodeSchemaIdentifier")
	@DynamoDBTypeConverted(converter = NodeSchemaIdentifierConverter::class)
	@DynamoDBAttribute(attributeName = "nodeSchemaIdentifier")
	var nodeSchemaIdentifier: NodeSchemaIdentifier? = null,

	//@JsonProperty("status")
	//val status: NodeStatus = NodeStatus.LINKED,
	//val associatedProperties: Set<AssociatedProperty>,

	@JsonProperty("itemType")
	@DynamoDBAttribute(attributeName = "itemType")
	override var itemType : String = "Node",

	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName = "createdAt")
	var createdAt: Long? = System.currentTimeMillis()

) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName = "updatedAt")
	var updatedAt: Long = System.currentTimeMillis()

	//override val entityID: String = id

	//override val sortKey: List<String> = listOf()//data.map{ element -> element.getID() }

}