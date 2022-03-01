package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class BlockMovementRequest(
    @JsonProperty("action")
    val action: String,

    @JsonProperty("blockID")
    val blockID: String,

    @JsonProperty("sourceNodeID")
    val sourceNodeID: String,

    @JsonProperty("destinationNodeID")
    val destinationNodeID: String
) : WDRequest
