package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidNodeID


@JsonIgnoreProperties(ignoreUnknown = true)
class TagRequest(

    val tagNames : Set<String>,

    val nodeID: String
) : WDRequest {
    init {

        require(nodeID.isValidNodeID()) { Messages.INVALID_NODE_ID }
    }

}