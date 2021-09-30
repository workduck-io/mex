package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.utils.Helper


@DynamoDBTable(tableName = "sampleData")
class Workspace(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName = "PK")
	var id: String = Helper.generateId(IdentifierType.WORKSPACE.name),


	@JsonProperty("idCopy")
	@DynamoDBRangeKey(attributeName = "SK")
	var idCopy: String = id,


	@JsonProperty("name")
	@DynamoDBAttribute(attributeName = "workspaceName")
	var name: String = "Sample Workspace Name"
) : Entity {

}