package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeNameRequest")
class NodeNameRequest (

    @JsonProperty("nodeToPathMap")
    val nodeToPathMap : Map<String, String>

) : WDRequest