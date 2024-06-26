package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidNamespaceID
import com.serverless.utils.extensions.isValidNodeID
import com.serverless.utils.extensions.isValidTitle
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.PageMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeRequest")
open class NodeRequest(

    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("referenceID")
    val referenceID: String? = null,

    @JsonProperty("title")
    val title: String = "",

    @JsonProperty("namespaceID")
    @JsonSerialize(converter = IdentifierSerializer::class)
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    var namespaceIdentifier: NamespaceIdentifier,

    @JsonProperty("data")
    val data: List<AdvancedElement>? = null,

    @JsonProperty("metadata")
    val pageMetadata: PageMetadata? = null,

    @JsonProperty("tags")
    var tags: MutableList<String> = mutableListOf(),
) : WDRequest {

    init {
        require(id.isValidNodeID() && referenceID?.isValidNodeID() ?: true) { "Invalid Node ID(s)" }

        require(namespaceIdentifier.id.isValidNamespaceID()) { "Invalid NamespaceID" }

        require(title.isNotEmpty()) { Messages.TITLE_REQUIRED }

        require(title.isValidTitle()) { Messages.INVALID_TITLE }
    }
}
