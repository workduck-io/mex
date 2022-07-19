package com.workduck.models

import com.fasterxml.jackson.annotation.JsonCreator

enum class ElementTypes(private val type: String) {
    ELEMENT_H1("h1"),
    ELEMENT_H2("h2"),
    ELEMENT_H3("h3"),
    ELEMENT_H4("h4"),
    ELEMENT_H5("h5"),
    ELEMENT_H6("h6"),
    ELEMENT_HR("hr"),
    ELEMENT_PARAGRAPH("paragraph"),
    ELEMENT_TAG("tag"),
    ELEMENT_TODO_LI("action_item"),
    ELEMENT_QA_BLOCK("agent-based-question"),
    ELEMENT_ILINK("ilink"),
    ELEMENT_MENTION("mention"),
    ELEMENT_ACTION_BLOCK("action-block"),
    ELEMENT_INLINE_BLOCK("inline_block"),
    ELEMENT_EXCALIDRAW("excalidraw"),
    ELEMENT_TABLE("table"),
    ELEMENT_TH("th"),
    ELEMENT_TR("tr"),
    ELEMENT_TD("td"),
    ELEMENT_CODE_BLOCK("code_block"),
    ELEMENT_CODE_LINE("code_line"),
    ELEMENT_CODE_SYNTAX("code_syntax"),
    ELEMENT_IMAGE("img"),
    ELEMENT_LINK("a"),
    ELEMENT_MEDIA_EMBED("media_embed"),
    ELEMENT_UL("ul"),
    ELEMENT_OL("ol"),
    ELEMENT_LI("li"),
    ELEMENT_LIC("lic"),
    ELEMENT_SYNC_BLOCK("sync_block"), // Probably deprecated
    MARK_HIGHLIGHT("highlight"),
    MARK_BOLD("bold"),
    MARK_CODE("code"),
    MARK_ITALIC("italic"),
    MARK_STRIKETHROUGH("strikethrough");

    companion object {
        private val codes = values().associateBy(ElementTypes::type)

        @JvmStatic
        @JsonCreator
        fun fromName(value: String): ElementTypes? = codes[value]
    }

    fun getType(): String {
        return this.type
    }
}
