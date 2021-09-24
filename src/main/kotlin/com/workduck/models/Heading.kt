package com.workduck.models

enum class HeadingType{
    H1,
    H2
}
data class Heading(
    val content: String,
    val type: HeadingType
): Element {
    override fun content(): String {
        TODO("Not yet implemented")
    }
    override fun getID(): String {
        TODO("Not yet implemented")
    }
    override fun getChildren(): List<Element> {
        TODO("Not yet implemented")
    }
    override fun getElementType(): String  {
        TODO("Not yet implemented")
    }
}

