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
@JsonTypeName("NodeRequest")
open class NodeRequest : WDRequest {

    @JsonProperty("lastEditedBy")
    open val lastEditedBy: String = ""

    @JsonProperty("id")
    open val id: String = ""

    @JsonProperty("referenceID")
    open val referenceID: String? = null

    @JsonProperty("title")
    open val title: String = "New Node"

    @JsonProperty("namespaceIdentifier")
    @JsonSerialize(converter = IdentifierSerializer::class)
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    open val namespaceIdentifier: NamespaceIdentifier? = null

    @JsonProperty("data")
    open val data: List<AdvancedElement>? = null

    @JsonProperty("tags")
    open var tags: MutableList<String>? = null
}
