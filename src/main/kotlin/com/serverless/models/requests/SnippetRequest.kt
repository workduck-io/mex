package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidSnippetID
import com.serverless.utils.extensions.isValidTitle
import com.workduck.models.AdvancedElement
import com.workduck.models.PageMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SnippetRequest")
data class SnippetRequest(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("data")
    val data: List<AdvancedElement>? = null,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("version")
    var version: Int = 1,

    @JsonProperty("template")
    val template: Boolean? = false,

    @JsonProperty("metadata")
    val pageMetadata: PageMetadata? = null,

) : WDRequest {

    init {
        require(id.isValidSnippetID()) {
            Messages.INVALID_SNIPPET_ID
        }

        require(title.isValidTitle()) {
            Messages.INVALID_TITLE
        }

        require(version > 0) {
            "Enter a valid version number"
        }
    }
}
