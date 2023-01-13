package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidNodeID


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

        require( if(existingNodePath.namespaceID == newNodePath.namespaceID) existingNodePath.path != newNodePath.path else true ) {
            "Old path and new path can't be same for same namespace"
        }


        require(nodeID.isValidNodeID()) {
            Messages.INVALID_NODE_ID
        }
    }
    
}