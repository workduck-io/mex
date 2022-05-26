package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.workduck.converters.HighlightMetadataConverter
import com.workduck.converters.SaveableRangeConverter
import javax.swing.text.Highlighter

enum class ElementTypes(val type: String) {

    PARAGRAPH("paragraph");

    companion object {
        private val codes = values().associateBy(ElementTypes::type)
        @JvmStatic
        @JsonCreator
        fun from(value: String) = codes[value]
    }
}


@JsonTypeInfo(
 	use = JsonTypeInfo.Id.DEDUCTION,
    defaultImpl = AdvancedElement::class
)
@JsonSubTypes(
    Type(HighlightedElement::class)
)
@JsonIgnoreProperties(ignoreUnknown = true)
open class AdvancedElement(

    @JsonProperty("id")
    var id: String = "defaultValue",

    @JsonProperty("content")
    var content: String? = "",

    @JsonProperty("children")
    var children: List<AdvancedElement>? = null,

    @JsonProperty("elementType")
    var elementType: String = "paragraph",

    @JsonProperty("properties")
    var properties: Map<String, Any>? = null,

    @JsonProperty("elementMetadata")
    var metadata : ElementMetadata ?= null,

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
