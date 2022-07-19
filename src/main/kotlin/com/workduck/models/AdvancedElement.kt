package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.AdvancedElementPropertiesConverter
import com.workduck.converters.AdvancedElementPropertiesDeserializer
import com.workduck.converters.AdvancedElementPropertiesSerializer
import com.workduck.converters.ElementTypesConverter
import com.workduck.converters.ElementTypesDeserializer
import com.workduck.converters.ElementTypesSerializer

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

    @JsonProperty("id")
    var id: String = "defaultValue",

    @JsonProperty("content")
    var content: String? = "",

    @JsonProperty("children")
    var children: List<AdvancedElement>? = null,

    @JsonProperty("elementType")
    @JsonDeserialize(converter = ElementTypesDeserializer::class)
    @JsonSerialize(converter = ElementTypesSerializer::class)
    @DynamoDBTypeConverted(converter = ElementTypesConverter::class)
    var elementType: ElementTypes = ElementTypes.ELEMENT_PARAGRAPH,

    @JsonProperty("properties")
    @JsonDeserialize(converter = AdvancedElementPropertiesDeserializer::class)
    @JsonSerialize(converter = AdvancedElementPropertiesSerializer::class)
    @DynamoDBTypeConverted(converter = AdvancedElementPropertiesConverter::class)
    var properties: Map<AdvancedElementProperties, Any>? = null,

    @JsonProperty("elementMetadata")
    var elementMetadata: ElementMetadata ? = null,

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

    // fun getProperties(): Map<String, Any>? = properties
}
