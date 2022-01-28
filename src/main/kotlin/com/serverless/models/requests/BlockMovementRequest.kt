package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonProperty

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
