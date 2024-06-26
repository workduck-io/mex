package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidNodeID
import com.serverless.utils.extensions.isValidTitle
import com.workduck.models.AdvancedElement
import com.workduck.models.PageMetadata


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("UpdateSharedNodeRequest")
data class UpdateSharedNodeRequest (
    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("title")
    val title: String = "",

    @JsonProperty("data")
    val data: List<AdvancedElement>? = null,

    @JsonProperty("metadata")
    val pageMetadata: PageMetadata? = null,

    @JsonProperty("tags")
    var tags: MutableList<String> = mutableListOf()
) : WDRequest {

    init {
        require(id.isValidNodeID() ) { Messages.INVALID_NODE_ID }

        require(title.isNotEmpty()) { Messages.TITLE_REQUIRED }

        require(title.isValidTitle()) { Messages.INVALID_TITLE }
    }

}

