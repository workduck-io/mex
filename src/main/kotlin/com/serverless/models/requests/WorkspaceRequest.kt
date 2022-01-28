package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("WorkspaceRequest")
data class WorkspaceRequest(

    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("name")
    val name: String = ""

) : WDRequest
