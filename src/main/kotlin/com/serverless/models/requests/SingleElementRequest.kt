package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleElementRequest(

    @JsonProperty("block")
    val block : AdvancedElement
) : WDRequest
