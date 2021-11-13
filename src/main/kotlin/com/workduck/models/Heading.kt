package com.workduck.models

enum class HeadingType {
    H1,
    H2
}

data class Heading(
    private var id: String = "",
    private var content: String = "",
    val type: HeadingType
) : Element
