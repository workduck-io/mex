package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement


@JsonIgnoreProperties(ignoreUnknown = true)
class ElementRequest(

        @JsonProperty("elements")
        val elements : List<AdvancedElement>
) : WDRequest
