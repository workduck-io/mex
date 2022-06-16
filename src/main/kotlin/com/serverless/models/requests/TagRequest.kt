package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants
import com.serverless.utils.isValidID


@JsonIgnoreProperties(ignoreUnknown = true)
class TagRequest(

    val tagNames : Set<String>,

    val nodeID: String
) : WDRequest {
    init {

        require(nodeID.isValidID(Constants.NODE_ID_PREFIX)) { "Invalid NodeID" }
    }

}