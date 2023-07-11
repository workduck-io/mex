package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
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
        property = "type",
        visible = true,
        defaultImpl = AdvancedElement::class
)
@JsonSubTypes(
        JsonSubTypes.Type(value = NodeILink::class, name = "nodeILink"),
        JsonSubTypes.Type(value = BlockILink::class, name = "blockILink"),
        JsonSubTypes.Type(value = WebLink::class, name = "webLink"),
)
open class AdvancedElement(

    @JsonProperty("id")
    var id: String = "defaultValue",

    @JsonProperty("content")
    var content: String? = "",

    @JsonProperty("children")
    var children: List<AdvancedElement>? = null,

    @JsonIgnore
    @JsonProperty("type")
    @JsonAlias("elementType")
    var elementType: String = "p",

    @JsonProperty("properties")
    var properties: Map<String, Any>? = null,

    @JsonIgnore
    @JsonProperty("elementMetadata")
    var elementMetadata : ElementMetadata ?= null,

    @JsonProperty("entityRefID")
    var entityRefID : String? = null,

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

    @JsonProperty("type")
    fun getType(): String {
        return this.elementType
    }

    @JsonProperty("metadata")
    fun getMetadata(): ElementMetadata ? {
        return this.elementMetadata
    }

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
