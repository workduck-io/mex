package com.serverless.models.requests

import com.workduck.models.AdvancedElement

data class EntityRequest(
    val id: String? = null,
    val data : AdvancedElement
)
