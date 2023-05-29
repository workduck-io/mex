package com.workduck.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
class BlockElement(
    val blockID: String,
    val entityID: String
) : Element {
    /**
     * Todo: Data to be loaded by the data loader instance which talks to entity db
     */
    fun content(data: AdvancedElement): String? =
        data.content
}