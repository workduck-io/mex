package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.utils.Helper


@DynamoDBTable(tableName="elementsTableTest")
class Workspace(

	@JsonProperty("id")
	@DynamoDBHashKey(attributeName="PK")
	var id : String = Helper.generateId("WS"),


	@JsonProperty("name")
	@DynamoDBAttribute(attributeName="workspaceName")
	var name : String = "Sample Workspace Name"
) : Entity {

}