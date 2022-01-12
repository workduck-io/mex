package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.AdvancedElement

@JsonTypeName("ElementRequest")
data class ElementRequest(

        @JsonProperty("elements")
        val elements : List<AdvancedElement>,
) : WDRequest
