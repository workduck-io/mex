package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SnippetRequest")
data class SnippetRequest(

    @JsonProperty("id")
    val id: String = "",

//    @JsonProperty("namespaceIdentifier")
//    @JsonSerialize(converter = IdentifierSerializer::class)
//    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
//    override val namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("data")
    override val data: List<AdvancedElement>? = null,

    @JsonProperty("title")
    override val title: String,

) : WDRequest, PageRequest
