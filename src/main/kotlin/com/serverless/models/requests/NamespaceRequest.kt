package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.serverless.utils.isValidTitle

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NamespaceRequest")
data class NamespaceRequest(
    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("name")
    val name: String = "",
) : WDRequest {

    init {
        require(id.isValidID(Constants.NAMESPACE_ID_PREFIX)) { "Invalid ID" }

        require(name.isNotEmpty()) { "Namespace name is required" }

        require(name.isValidTitle()) { "Invalid Title" }
    }
}
