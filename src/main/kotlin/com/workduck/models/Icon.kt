package com.workduck.models

enum class IconType {
    URL,
    ICON,
    EMOJI
}

data class Icon(


    val type: IconType? = null,

    val value: String? = null,

) {
    init {
        check(ensureBothExistOrNull(type, value)) { "One of the fields for Icon are missing" }
    }

    private fun ensureBothExistOrNull(type: IconType?, value: String?): Boolean {
        return (type != null && value != null) || (type == null && value == null)
    }
}
