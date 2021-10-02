package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize


enum class ElementTypes(val type: String) {

	PARAGRAPH("paragraph");

	companion object {
		private val codes = ElementTypes.values().associateBy(ElementTypes::type)
		@JvmStatic
		@JsonCreator
		fun from(value: String) = codes[value]
	}
}

class AdvancedElement(

	@JsonProperty("id")
	private var id: String = "defaultValue",

	@JsonProperty("type")
	private var type: String? = "AdvancedElement",

	@JsonProperty("parentID")
	private var parentID: String? = null,

	@JsonProperty("content")
	private var content: String? = null,

	@JsonProperty("childrenElements")
	private var children: List<Element>? = listOf(),

	@JsonProperty("elementType")
	private var elementType: String? = "paragraph"
) : Element {
	override fun getContent(): String {
		if (content != null) return content as String
		return ""
	}

	override fun getID(): String = id

	override fun getChildren(): List<Element>? = children

	fun getElementType(): String? = elementType

	override fun getType(): String? = type

}