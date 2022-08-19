package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class GenericListRequest(
    @JsonProperty("ids")
    val ids: List<String>
) : WDRequest {
    init {
        require(ids.isNotEmpty()) { "Provide IDs" }
    }
}
