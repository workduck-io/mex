package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SnippetRequest")
data class SnippetRequest(

    @JsonProperty("lastEditedBy")
    override val lastEditedBy: String = "",

    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("namespaceIdentifier")
    @JsonSerialize(converter = IdentifierSerializer::class)
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    override val namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("data")
    override val data: List<AdvancedElement>? = null,

) : WDRequest, PageRequest
