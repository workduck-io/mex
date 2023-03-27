package com.workduck.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
class BlockElement(
    val blockID: String,
    val captureID: String,
    val configID: String
) : Element {
    /**
     * Todo: Data to be loaded by the data loader instance which talks to entity db
     */
    fun content(data: AdvancedElement): String? =
        data.content
}