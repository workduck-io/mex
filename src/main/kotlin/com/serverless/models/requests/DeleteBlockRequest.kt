package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class DeleteBlockRequest (
    @JsonProperty("blockID")
    val blockIds: List<String>,

    @JsonProperty("nodeID")
    val nodeID: String,
) : WDRequest