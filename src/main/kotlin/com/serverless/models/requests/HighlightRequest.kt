package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("HighlightRequest")
data class HighlightRequest (
    @JsonProperty("nodeNamespaceMap")
    val nodeNamespaceMap: NodeNamespaceMap ? = null,

    // rather than a list, a single Advanced Element
    @JsonProperty("data")
    val data: AdvancedElement,

    @JsonProperty("id")
    val id: String? = null,
) : WDRequest