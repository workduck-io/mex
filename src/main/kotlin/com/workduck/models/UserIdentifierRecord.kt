package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.*
import com.workduck.utils.Helper


@DynamoDBTable(tableName = "sampleData")
class UserIdentifierRecord(

	@JsonProperty("userID")
	@DynamoDBHashKey(attributeName = "PK")
	var userID : String = Helper.generateId("USER"),

	@JsonProperty("identifier")
	@DynamoDBRangeKey(attributeName = "SK")
	@JsonDeserialize(converter = IdentifierDeserializer::class)
	@JsonSerialize(converter = IdentifierSerializer::class)
	@DynamoDBTypeConverted(converter = IdentifierConverter::class)
	var identifier : Identifier?= null,

	@JsonProperty("createdAt")
	@DynamoDBAttribute(attributeName = "createdAt")
	var createdAt: Long? = System.currentTimeMillis()
) : Entity {

	@JsonProperty("updatedAt")
	@DynamoDBAttribute(attributeName = "updatedAt")
	var updateAt: Long = System.currentTimeMillis()

}