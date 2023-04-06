package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidNamespaceID
import com.serverless.utils.extensions.isValidTitle
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
        require(id.isValidNamespaceID() ) { "Invalid NamespaceID" }

        require(name.isNotEmpty()) { "Namespace name is required" }

        require(name.isValidTitle()) { Messages.INVALID_TITLE }
    }
}
