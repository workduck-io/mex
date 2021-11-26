package com.serverless.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.workduck.models.AdvancedElement

class ElementRequest(

        @JsonProperty("elements")
        val elements : List<AdvancedElement>
) : WDRequest
