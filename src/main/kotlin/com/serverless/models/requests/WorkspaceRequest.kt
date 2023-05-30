package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.WorkspaceMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("WorkspaceRequest")
data class WorkspaceRequest(

    @JsonProperty("name")
    val name: String,

    @JsonProperty("workspaceMetadata")
    val workspaceMetadata: WorkspaceMetadata? = null,
) : WDRequest
