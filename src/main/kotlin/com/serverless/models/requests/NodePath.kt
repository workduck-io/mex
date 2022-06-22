package com.serverless.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.serverless.utils.Constants
import com.serverless.utils.getListOfNodes
import com.serverless.utils.getNewPath
import com.serverless.utils.isValidID
import com.workduck.utils.NodeHelper.getIDPath
import com.workduck.utils.NodeHelper.getNamePath

/**
 * Node path DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NodePath(
        var path: String,
        val namespaceID: String? = null
) {
    val allNodesNames = getNamePath(path).getListOfNodes().toMutableList()

    val allNodesIDs = getIDPath(path).getListOfNodes().toMutableList()

    init {
        require(path.isNotBlank()) {
            "Path cannot be empty"
        }

        require(allNodesIDs.none { nodeID -> !nodeID.isValidID(Constants.NODE_ID_PREFIX) }) {
            "One or more node ids passed are invalid"
        }

        require(allNodesIDs.size == allNodesNames.size) {
            "Invalid path format"
        }
    }
}
