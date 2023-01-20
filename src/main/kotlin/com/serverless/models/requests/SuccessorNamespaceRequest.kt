package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Messages
import com.serverless.utils.isValidNamespaceID

@JsonIgnoreProperties(ignoreUnknown = true)
data class SuccessorNamespaceRequest(

    val successorNamespaceID : String?
) : WDRequest {
    init{
        require(if(!successorNamespaceID.isNullOrEmpty()) successorNamespaceID.isValidNamespaceID() else true){ Messages.INVALID_NAMESPACE_ID }
    }
}