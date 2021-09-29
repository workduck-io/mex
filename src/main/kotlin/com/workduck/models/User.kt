package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.NamespaceIdentifierConverter
import com.workduck.converters.NamespaceIdentifierListConverter
import com.workduck.converters.WorkspaceIdentifierConverter
import com.workduck.converters.WorkspaceIdentifierListConverter
import com.workduck.utils.Helper


@DynamoDBTable(tableName="sampleData")
class User(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName="PK")
	var id : String = Helper.generateId(IdentifierType.USER.name),


	/* Can't create multiple entries with same PK,SK so keeping SK unique */
	@JsonProperty("uniqueID")
	@DynamoDBRangeKey(attributeName="SK")
	var uniqueID : String = Helper.generateId("$id#"),

	@JsonProperty("name")
	@DynamoDBAttribute(attributeName="userName")
	var name : String = "John Doe",


	@JsonProperty("workspaceIdentifier")
	@DynamoDBTypeConverted(converter = WorkspaceIdentifierConverter::class)
	@DynamoDBAttribute(attributeName="workspaceIdentifier")
	var workspaceIdentifier: WorkspaceIdentifier?= null,


	@JsonProperty("namespaceIdentifier")
	@DynamoDBTypeConverted(converter = NamespaceIdentifierConverter::class)
	@DynamoDBAttribute(attributeName="namespaceIdentifier")
	var namespaceIdentifier: NamespaceIdentifier?= null,


	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName="createdAt")
	var createdAt: Long = System.currentTimeMillis()
) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName="updatedAt")
	var updatedAt : Long = System.currentTimeMillis()
}