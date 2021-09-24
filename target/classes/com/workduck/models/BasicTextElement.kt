package com.workduck.models

import com.fasterxml.jackson.annotation.JsonProperty

class BasicTextElement(

    @JsonProperty("id")
    private var id: String = "defaultBSE",

    @JsonProperty("type")
    private var type: String? = null,

    @JsonProperty("content")
    private var content: String = "",

    @JsonProperty("properties")
    val properties: Set<TextElementProperties> ?= null
) : Element {
    override fun content(): String  = content
    override fun getID(): String = id
    override fun getChildren(): List<Element> = listOf()
    override fun getElementType(): String = "BASIC_TEXT_ELEMENT"
}