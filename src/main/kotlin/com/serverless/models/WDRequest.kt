package com.serverless.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
 	use = JsonTypeInfo.Id.NAME,
 	include = JsonTypeInfo.As.PROPERTY,
 	property = "type",
 )
 @JsonSubTypes(
 	JsonSubTypes.Type(value = NodeRequest::class, name = "NodeRequest")
 )
interface WDRequest {
}