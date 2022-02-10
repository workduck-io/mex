package com.serverless.models.responses

import com.workduck.models.AdvancedElement

class NodeElementsResponse (
    val endCursor: String? = null,

    val data: List<AdvancedElement> ? = null
) : Response