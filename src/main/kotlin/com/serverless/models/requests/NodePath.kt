package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants

/**
 * Node path DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NodePath(
        val path: String,
        val namespaceID: String? = null
) {
    val allNodes = path
            .split(Constants.DELIMITER)

    init {
        require(path.isNotBlank()) {
            "Path cannot be empty"
        }
    }
}
