package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.ItemTypeConverter
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.utils.Helper


@DynamoDBTable(tableName = "sampleData")
class Workspace(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName = "PK")
	var id: String = Helper.generateId(IdentifierType.WORKSPACE.name),

	/* For convenient deletion */
	@JsonProperty("idCopy")
	@DynamoDBRangeKey(attributeName = "SK")
	var idCopy: String = id,


	@JsonProperty("name")
	@DynamoDBAttribute(attributeName = "workspaceName")
	var name: String = "DEFAULT_WORKSPACE",


	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName = "createdAt")
	var createdAt: Long? = System.currentTimeMillis(),

	@JsonProperty("itemType")
	@DynamoDBAttribute(attributeName = "itemType")
	@DynamoDBTypeConverted(converter = ItemTypeConverter::class)
	override var itemType: String = "Workspace"

) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName = "updatedAt")
	var updatedAt: Long = System.currentTimeMillis()

}