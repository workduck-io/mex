package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("UpdateSnippetVersionRequest")
data class UpdateSnippetVersionRequest(
    val id: String,

    val data: List<AdvancedElement>?,

    val title: String,

    val version: Long
) : WDRequest {

    init {
        require(version > 0) {
            "Enter a valid version number"
        }
    }
}
