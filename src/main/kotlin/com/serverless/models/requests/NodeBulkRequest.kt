package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.AdvancedElement
import com.workduck.models.PageMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeBulkRequest")
data class NodeBulkRequest(

    val nodePath: NodePath, // nodePath contains path and namespaceID

    val data: List<AdvancedElement>? = null,

    var tags: MutableList<String> = mutableListOf(),

    @JsonProperty("metadata")
    val pageMetadata: PageMetadata? = null,

    ) : WDRequest
