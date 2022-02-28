package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.workduck.converters.IdentifierSerializer
import com.workduck.converters.NamespaceIdentifierDeserializer
import com.workduck.converters.WorkspaceIdentifierDeserializer
import com.workduck.models.AdvancedElement
import com.workduck.models.NamespaceIdentifier
import com.workduck.models.WorkspaceIdentifier

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeRequest(

    @JsonProperty("lastEditedBy")
    val lastEditedBy: String = "",

    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("nodePath")
    val nodePath: String ?= null,

    @JsonProperty("parentNodeID")
    val referenceID: String ?= null,

    @JsonProperty("title")
    val title: String = "New Node",

    @JsonProperty("namespaceIdentifier")
    @JsonSerialize(converter = IdentifierSerializer::class)
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    val namespaceIdentifier: NamespaceIdentifier? = null,

//    @JsonProperty("workspaceIdentifier")
//    @JsonSerialize(converter = IdentifierSerializer::class)
//    @JsonDeserialize(converter = WorkspaceIdentifierDeserializer::class)
//    val workspaceIdentifier: WorkspaceIdentifier? = null,

    @JsonProperty("data")
    val data: List<AdvancedElement>? = null,

    @JsonProperty("tags")
    var tags: MutableList<String>? = null,

) : WDRequest
