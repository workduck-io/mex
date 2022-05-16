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
import com.workduck.models.SaveableRange

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeRequest")
open class NodeRequest(

    @JsonProperty("id")
    val id: String = "",

    @JsonProperty("referenceID")
    val referenceID: String? = null,

    @JsonProperty("title")
    val title: String = "",

    @JsonProperty("namespaceIdentifier")
    @JsonSerialize(converter = IdentifierSerializer::class)
    @JsonDeserialize(converter = NamespaceIdentifierDeserializer::class)
    var namespaceIdentifier: NamespaceIdentifier? = null,

    @JsonProperty("data")
    val data: List<AdvancedElement>? = null,

    @JsonProperty("saveableRange")
    var saveableRange: SaveableRange? = null,

    @JsonProperty("sourceUrl")
    var sourceUrl: String? = null,

    @JsonProperty("tags")
    var tags: MutableList<String> = mutableListOf(),
) : WDRequest {

    init {
        require(id.isNotEmpty()) { "ID is required" }
    }

    init {
        require(title.isNotEmpty()) { "Title is required" }
    }
}
