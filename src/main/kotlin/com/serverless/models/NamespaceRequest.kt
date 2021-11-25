package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("NamespaceRequest")
data class NamespaceRequest(
    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("name")
    val name: String = "",

    @JsonProperty("name")
    val workspaceID: String = ""
) : WDRequest
