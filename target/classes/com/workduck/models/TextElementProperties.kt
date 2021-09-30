package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.google.common.base.Preconditions

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type"
)
@JsonSubTypes(
	JsonSubTypes.Type(value = Bold::class, name = "bold")

)
sealed class TextElementProperties

@JsonTypeName("bold")
data class Bold(
	val level: Int
) : TextElementProperties() {
	init {
		Preconditions.checkArgument(level in 100..700) {
			"Boldness should be between 100 and 700"
		}
	}
}