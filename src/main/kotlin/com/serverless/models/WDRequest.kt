package com.serverless.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
 	use = JsonTypeInfo.Id.NAME,
 	include = JsonTypeInfo.As.PROPERTY,
 	property = "type",
 	visible = true
 )
 @JsonSubTypes(
 	JsonSubTypes.Type(value = NodeRequest::class, name = "Node"),
 )
interface WDRequest {

	fun getType() : String
}