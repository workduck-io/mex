package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeBulkRequest")
data class NodeBulkRequest(

    val nodePath: NodePath,

    val data: List<AdvancedElement>? = null,

    var tags: MutableList<String> = mutableListOf(),

) : WDRequest
