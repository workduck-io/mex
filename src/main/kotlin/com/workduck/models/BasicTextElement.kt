package com.workduck.models

class BasicTextElement(
    private val content: String,
    val properties: Set<TextElementProperties>
) : Element {
    override fun content(): String  = content
}