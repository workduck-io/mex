package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID
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

) : WDRequest {

    init {
        require(id.isValidID(Constants.NODE_ID_PREFIX) && referenceID?.isValidID(Constants.NODE_ID_PREFIX) ?: true) { "Invalid ID(s)" }

        require(title.isNotEmpty()) { "Title is required" }
    }

}
