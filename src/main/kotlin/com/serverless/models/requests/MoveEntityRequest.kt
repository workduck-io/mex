package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonProperty

data class MoveEntityRequest(
    @JsonProperty("nodeNamespaceMap")
    val nodeNamespaceMap: NodeNamespaceMap,

    // rather than a list, a single Advanced Element
    @JsonProperty("entityID")
    val entityID: String,
) : WDRequest


