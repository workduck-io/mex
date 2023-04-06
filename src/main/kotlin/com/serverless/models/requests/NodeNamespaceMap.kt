package com.serverless.models.requests

import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidNamespaceID
import com.serverless.utils.extensions.isValidNodeID

data class NodeNamespaceMap(

    val nodeID: String,
    val namespaceID: String
) {
    init {

        require(nodeID.isValidNodeID()){
            Messages.INVALID_NODE_ID
        }

        require(namespaceID.isValidNamespaceID()){
            Messages.INVALID_NAMESPACE_ID
        }
    }
}