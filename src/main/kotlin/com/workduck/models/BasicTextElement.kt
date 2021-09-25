package com.workduck.models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.fasterxml.jackson.annotation.JsonProperty

class BasicTextElement(

    @JsonProperty("id")
    private var id: String = "defaultBSE",

    @JsonProperty("type")
    private var type: String? = "BasicTextElement",

    @JsonProperty("content")
    private var content: String = "",

    @JsonProperty("children")
    private var children: List<Element>? = null,

    @JsonProperty("properties")
    val properties: Set<TextElementProperties> ?= null
) : Element {
    override fun getContent(): String  = content
    override fun getID(): String = id
    override fun getChildren(): List<Element>? = children
    //fun getElementType(): String = "BASIC_TEXT_ELEMENT"
    override fun getType() : String? = type
}