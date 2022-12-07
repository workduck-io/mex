package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.serverless.utils.isValidTitle
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
        require(id.isValidID(Constants.NODE_ID_PREFIX) ) { "Invalid Node ID(s)" }

        require(title.isNotEmpty()) { "Title is required" }

        require(title.isValidTitle()) { "Invalid Title" }
    }

}

