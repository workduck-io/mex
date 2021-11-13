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
    private var content: String = "",

    @JsonProperty("childrenElements")
    private var children: List<AdvancedElement>? = listOf(),

    @JsonProperty("elementType")
    private var elementType: String? = "paragraph",

    @JsonProperty("properties")
    private var properties: Map<String, Any>? = null,

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


    fun getProperties(): Map<String, Any>? = properties

    /* Generated equals() and hashcode() functions */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdvancedElement

        if (id != other.id) return false
        if (parentID != other.parentID) return false
        if (content != other.content) return false
        if (children != other.children) return false
        if (elementType != other.elementType) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (parentID?.hashCode() ?: 0)
        result = 31 * result + (content.hashCode() ?: 0)
        result = 31 * result + (children?.hashCode() ?: 0)
        result = 31 * result + (elementType?.hashCode() ?: 0)
        result = 31 * result + (properties?.hashCode() ?: 0)
        return result
    }


}
