package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NamespaceRequest")
data class NamespaceRequest(
    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("name")
    val name: String = "",

    @JsonProperty("name")
    val workspaceID: String = ""
) : WDRequest
