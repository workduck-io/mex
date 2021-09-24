package com.workduck.models

import com.fasterxml.jackson.annotation.JsonProperty


enum class ElementTypes(val type : String) {
	PARAGRAPH("paragraph"),
	LIST("list")
}

class AdvancedElement (

	@JsonProperty("id")
	private var id: String = "defaultValue",

	@JsonProperty("type")
	private var type : String? = null,

	@JsonProperty("parentID")
	private var parentID : String? = null,

	@JsonProperty("content")
	private var content: String? = null,

	@JsonProperty("childrenElements")
	private var childrenElements: List<Element> = listOf(),

	@JsonProperty("elementType")
	private var elementType : Enum<ElementTypes>? = ElementTypes.PARAGRAPH,
) : Element {
	override fun content(): String {
		if (content != null) return content as String
		return ""
	}

	override fun getID(): String = id

	override fun getChildren(): List<Element> = childrenElements

	override fun getElementType(): String = "Testing"
}