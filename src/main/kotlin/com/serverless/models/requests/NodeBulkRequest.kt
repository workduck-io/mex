package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.workduck.models.AdvancedElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("NodeBulkRequest")
data class NodeBulkRequest(

    val id: String = "",

    val nodePath: NodePath,

    val referenceID: String ? = null,

    val title: String,

    val data: List<AdvancedElement>? = null,

    var tags: MutableList<String> = mutableListOf(),

) : WDRequest{

    init {
        require(id.isNotEmpty()) { "ID is required" }
    }

    init {
        require(title.isNotEmpty()) { "Title is required" }
    }
}

