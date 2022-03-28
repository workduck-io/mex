package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants


/**
 * Node path dto
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

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("RefactorRequest")
data class RefactorRequest(

    @JsonProperty("existingNodePath")
    val existingNodePath: NodePath,

    @JsonProperty("newNodePath")
    val newNodePath: NodePath,

    @JsonProperty("lastEditedBy")
    val lastEditedBy: String,

    /* id of the last node in existingNodePath */
    @JsonProperty("nodeID")
    val nodeID: String

) : WDRequest {


    init {
        // assuming single workspace
        require(existingNodePath != newNodePath) {
            "Old path and new path can't be same"
        }

        require(existingNodePath.namespaceID == newNodePath.namespaceID) {
            "Movement across namespace is not supported yet"
        }
    }

    // See the assumption above
    val namespaceID = existingNodePath.namespaceID
}