package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.converters.NamespaceIdentifierListConverter
import com.workduck.converters.WorkspaceIdentifierListConverter


@DynamoDBTable(tableName="elementsTableTest")
class User(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName="PK")
	var id : String = "",

	@JsonProperty("name")
	@DynamoDBAttribute(attributeName="userName")
	var name : String = "John Doe",


	@JsonProperty("workspaces")
	@DynamoDBTypeConverted(converter = WorkspaceIdentifierListConverter::class)
	@DynamoDBAttribute(attributeName="workspaces")
	var workspaceIdentifiers: MutableList<WorkspaceIdentifier>?= null,


	@JsonProperty("namespaces")
	@DynamoDBTypeConverted(converter = NamespaceIdentifierListConverter::class)
	@DynamoDBAttribute(attributeName="namespaces")
	var namespaceIdentifiers: MutableList<NamespaceIdentifier>?= null,


	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName="createdAt")
	var createdAt: Long = System.currentTimeMillis()
) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName="updatedAt")
	var updatedAt : Long = System.currentTimeMillis()
}