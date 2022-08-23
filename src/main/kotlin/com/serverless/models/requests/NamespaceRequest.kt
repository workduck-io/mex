package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.isValidTitle

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NamespaceRequest")
data class NamespaceRequest(
    val name: String,
) : WDRequest {

    init {
        require(name.isNotEmpty()) { "Namespace name is required" }

        require(name.isValidTitle()) { "Invalid Title" }
    }
}
