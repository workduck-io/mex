package com.workduck.models.externalRequests

import com.fasterxml.jackson.annotation.JsonProperty

data class ExternalRequestHeader(
    @JsonProperty("mex-workspace-id")
    val workspaceID : String,

    @JsonProperty("mex-user-id")
    val userID : String,

    @JsonProperty("external-source")
    val externalSource : String? = "mex-backend"
)