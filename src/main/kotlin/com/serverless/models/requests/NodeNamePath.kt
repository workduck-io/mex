package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidTitle

/**
 * Node path DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeNamePath(
        var path: String,
        val namespaceID: String
) {
    val allNodes = path
            .split(Constants.DELIMITER).toMutableList()

    init {
        require(path.isNotBlank()) {
            "Path cannot be empty"
        }

        require(allNodes.none { title -> !title.isValidTitle() }) {
            Messages.INVALID_TITLE
        }
    }
}
