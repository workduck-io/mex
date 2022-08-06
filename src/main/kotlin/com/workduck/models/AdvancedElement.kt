package com.workduck.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


enum class ElementTypes(val type: String) {

    PARAGRAPH("paragraph");

    companion object {
        private val codes = values().associateBy(ElementTypes::type)
        @JvmStatic
        @JsonCreator
        fun from(value: String) = codes[value]
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "elementType",
        visible = true,
        defaultImpl = AdvancedElement::class
)
@JsonSubTypes(
        JsonSubTypes.Type(value = NodeILink::class, name = "nodeILink"),
        JsonSubTypes.Type(value = BlockILink::class, name = "blockILink"),
        JsonSubTypes.Type(value = WebLink::class, name = "webLink"),
)
open class AdvancedElement(

    var id: String = "defaultValue",

    var content: String? = "",

    var children: List<AdvancedElement>? = null,

    var elementType: String = "p",

    var properties: Map<String, Any>? = null,

    var elementMetadata : ElementMetadata ?= null,

    var createdBy: String? = null,

    var lastEditedBy: String? = null,

    var createdAt: Long? = null

) : Element {

    /* a certain block may not always be updated  */
    var updatedAt: Long? = null

    /* Generated equals() and hashcode() functions */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdvancedElement

        if (id != other.id) return false
        if (content != other.content) return false
        if (children != other.children) return false
        if (elementType != other.elementType) return false
        if (properties != other.properties) return false

        return true
    }

    //fun getProperties(): Map<String, Any>? = properties
}
