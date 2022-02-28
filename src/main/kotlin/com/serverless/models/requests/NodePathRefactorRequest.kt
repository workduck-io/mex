package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodePathRefactorRequest")
class NodePathRefactorRequest (

    @JsonProperty("currentParentID")
    val currentParentID : String,

    @JsonProperty("newParentID")
    val newParentID : String,

    @JsonProperty("nodeID")
    val nodeID : String

) : WDRequest