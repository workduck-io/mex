package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("WorkspaceRequest")
data class WorkspaceRequest(

    @JsonProperty("name")
    val name: String

) : WDRequest
