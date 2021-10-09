package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.JsonNode
import com.workduck.utils.Helper

/**
 * All associated property type
 */
enum class AssociatedPropertyType {
	TAG
}

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type"
)
@JsonSubTypes(
	JsonSubTypes.Type(value = Tag::class, name = "tag")
)
sealed class AssociatedProperty(
	propertyType: AssociatedPropertyType
)

@JsonTypeName("tag")
data class Tag(
	val id: String = Helper.generateId("TAG"),
	val name: String,
	val ownerIdentifier: OwnerIdentifier,
	val expireAt: Long? = null,
	val metaData: JsonNode? = null,
	override val itemType: String = "Tag"

) : AssociatedProperty(AssociatedPropertyType.TAG), Entity {
//    override val partitionKey: String
//        get() = TODO("Not yet implemented")
//
//    override val sortKey: List<String>
//        get() = TODO("Not yet implemented")
}