package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.serverless.utils.isValidTitle
import com.workduck.models.NamespaceMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NamespaceRequest")
data class NamespaceRequest(
    val id: String,

    val name: String,

    @JsonProperty("metadata")
    val namespaceMetadata: NamespaceMetadata? = null,
) : WDRequest {

    init {
        require(id.isValidID(Constants.NAMESPACE_ID_PREFIX) ) { "Invalid NamespaceID" }

        require(name.isNotEmpty()) { "Namespace name is required" }

        require(name.isValidTitle()) { "Invalid Title" }
    }
}
