package com.workduck.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = AdvancedElement::class, name = "AdvancedElement"),
    JsonSubTypes.Type(value = BasicTextElement::class, name = "BasicTextElement")
)
interface Element {
    fun content(): String
    fun getID() : String
    fun getChildren() : List<Element>
    fun getElementType() : String
}