package com.workduck.models

// @JsonTypeInfo(
// 	use = JsonTypeInfo.Id.NAME,
// 	include = JsonTypeInfo.As.PROPERTY,
// 	property = "type",
// 	visible = true
// )
// @JsonSubTypes(
// 	JsonSubTypes.Type(value = AdvancedElement::class, name = "AdvancedElement"),
// 	JsonSubTypes.Type(value = BasicTextElement::class, name = "BasicTextElement")
// )
interface Element {
    //fun getContent(): String
    //fun getID(): String
    //fun getChildren(): List<Element>?
    // fun getType(): String?
    // fun getElementType() : String
}
