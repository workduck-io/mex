package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("RegisterWorkspaceRequest")
class RegisterWorkspaceRequest(
    @JsonProperty("userID")
    val userID: String,

    @JsonProperty("workspaceName")
    val workspaceName: String,
) : WDRequest
