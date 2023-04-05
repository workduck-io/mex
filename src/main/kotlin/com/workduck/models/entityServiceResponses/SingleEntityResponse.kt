package com.workduck.models.entityServiceResponses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleEntityResponse(
    val id : String,
    val data : AdvancedElement
)
