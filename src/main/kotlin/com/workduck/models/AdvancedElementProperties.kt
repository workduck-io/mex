package com.workduck.models

import com.fasterxml.jackson.annotation.JsonCreator


enum class AdvancedElementProperties(private val type: String) {

    BOLD("bold"),
    ITALIC("italic"),
    UNDERLINE("underline"),
    HIGHLIGHT("highlight"),
    CODE("code"),
    EMAIL("email"),
    URL("url"),
    VALUE("value"),
    BLOCKVALUE("blockValue"),
    CHECKED("checked"),
    BLOCKID("blockId"),
    BODY("body"),
    QUESTIONID("questionId"),
    QUESTION("question"),
    ANSWER("answer"),
    ACTIONCONTEXT("actionContext"),
    BLOCKMETA("blockMeta");

    companion object {
        private val codes = values().associateBy(AdvancedElementProperties::type)

        @JvmStatic
        @JsonCreator
        fun fromName(value: String) : AdvancedElementProperties? = codes[value]
    }

    fun getType() :String {
        return this.type
    }

}
