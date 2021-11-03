package com.workduck.models

import com.fasterxml.jackson.annotation.*

enum class ElementTypes(val type: String) {

    PARAGRAPH("paragraph");

    companion object {
        private val codes = ElementTypes.values().associateBy(ElementTypes::type)
        @JvmStatic
        @JsonCreator
        fun from(value: String) = codes[value]
    }
}

data class AdvancedElement(

    @JsonProperty("id")
    private var id: String = "defaultValue",

// 	@JsonProperty("type")
// 	private var type: String? = "AdvancedElement",

    @JsonProperty("parentID")
    private var parentID: String? = null,

    @JsonProperty("content")
    private var content: String? = null,

    @JsonProperty("childrenElements")
    private var children: List<AdvancedElement>? = listOf(),

    @JsonProperty("elementType")
    private var elementType: String? = "paragraph",

    @JsonProperty("properties")
    private var properties: Map<String, Any>? = null,

    /* don't consider createdBy,lastEditedBy, createdAt, updatedAt */
    @JsonProperty("hashCode")
    var hashCode: Int? = null,

    @JsonProperty("createdBy")
    var createdBy: String? = null,

    @JsonProperty("lastEditedBy")
    var lastEditedBy: String? = null,

    @JsonProperty("createdAt")
    var createdAt: Long? = null

) : Element {

    /* a certain block may not always be updated  */
    @JsonProperty("updatedAt")
    var updatedAt: Long? = null

    override fun getContent(): String {
        if (content != null) return content as String
        return ""
    }

    override fun getID(): String = id

    override fun getChildren(): List<Element>? = children

    fun getElementType(): String? = elementType

    // override fun getType(): String? = type

    fun getProperties(): Map<String, Any>? = properties
}
