package com.workduck.models.entityServiceResponses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MultipleEntityPaginatedResponse<T>(

    val Items : List<T>,
    val lastKey : String? = null
)