package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.utils.Helper

/**
 * namespace status
 */
enum class NamespaceStatus {
	ACTIVE,
	INACTIVE
}

/**
 * class for namespace
 */

@DynamoDBTable(tableName = "sampleData")
class Namespace(
	//val authorizations : Set<Auth>,

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName = "PK")
	var id: String = Helper.generateId(IdentifierType.NAMESPACE.name),

	/* For convenient deletion */
	@JsonProperty("idCopy")
	@DynamoDBRangeKey(attributeName = "SK")
	var idCopy: String = id,


	@JsonProperty("workspaceIdentifier")
	@JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
	@JsonSerialize(converter = IdentifierSerializer::class)
	@DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
	@DynamoDBAttribute(attributeName = "workspaceIdentifier")
	var workspaceIdentifier: WorkspaceIdentifier? = null,

	@JsonProperty("name")
	@DynamoDBAttribute(attributeName = "namespaceName")
	var name: String ? = null,

	//val owner: OwnerIdentifier,

	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName = "createdAt")
	var createdAt: Long? = System.currentTimeMillis(),

	@JsonProperty("itemType")
	@DynamoDBAttribute(attributeName = "itemType")
	override val itemType: String = "Workspace"


	//val status: NamespaceStatus = NamespaceStatus.ACTIVE

) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName = "updatedAt")
	var updatedAt = System.currentTimeMillis()
}