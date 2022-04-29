package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("RefactorRequest")
data class RefactorRequest(

    @JsonProperty("existingNodePath")
    val existingNodePath: NodePath,

    @JsonProperty("newNodePath")
    val newNodePath: NodePath,

    /* id of the last node in existingNodePath */
    @JsonProperty("nodeID")
    val nodeID: String

) : WDRequest {

    init {
        // assuming single workspace
        require(existingNodePath.path != newNodePath.path) {
            "Old path and new path can't be same"
        }

        require(existingNodePath.namespaceID == newNodePath.namespaceID) {
            "Movement across namespace is not supported yet"
        }
    }

    // See the assumption above
    val namespaceID = existingNodePath.namespaceID
}