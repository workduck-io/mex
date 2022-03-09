package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodePathRefactorRequest")
class RefactorNodePathRequest (

    @JsonProperty("existingNodePath")
    val existingNodePath: String,

    @JsonProperty("newNodePath")
    val newNodePath: String,

    @JsonProperty("lastEditedBy")
    val lastEditedBy: String,

    @JsonProperty("nodeID")
    val nodeID : String,

    @JsonProperty("namespaceID")
    val namespaceID : String?


) : WDRequest