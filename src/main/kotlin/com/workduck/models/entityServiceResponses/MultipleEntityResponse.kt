package com.workduck.models.entityServiceResponses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonIgnoreProperties(ignoreUnknown = true)
data class MultipleEntityResponse(

    val entities : List<SingleEntityResponse>
)