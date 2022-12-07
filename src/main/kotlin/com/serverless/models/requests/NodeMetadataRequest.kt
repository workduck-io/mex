package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.PageMetadata

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("MetadataRequest")
data class MetadataRequest(

    @JsonProperty("metadata")
    val pageMetadata: PageMetadata ?= null
) : WDRequest