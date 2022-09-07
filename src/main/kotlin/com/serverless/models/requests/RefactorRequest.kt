package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.isValidID


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("RefactorRequest")
data class RefactorRequest(

    @JsonProperty("existingNodePath")
    val existingNodePath: NodeNamePath,

    @JsonProperty("newNodePath")
    val newNodePath: NodeNamePath,

    /* id of the last node in existingNodePath */
    @JsonProperty("nodeID")
    val nodeID: String

) : WDRequest {

    init {
        // assuming single workspace
        require(existingNodePath.path != newNodePath.path) {
            "Old path and new path can't be same"
        }


        require(nodeID.isValidID(Constants.NODE_ID_PREFIX)) {
            "Invalid NodeID"
        }
    }
    
}