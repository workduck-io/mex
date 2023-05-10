package com.serverless.utils.extensions

import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.workduck.models.Bold
import com.workduck.utils.NodeHelper

fun Input.getNodeIDFromQueryParam(): String? = this.queryStringParameters?.let { map ->
    map[Constants.NODE_ID]?.also { nodeID ->
        require(nodeID.isValidNodeID()) { Messages.INVALID_NODE_ID }
    }
}

fun Input.getNamespaceIDFromQueryParam(): String? = this.queryStringParameters?.let { map ->
    map[Constants.NAMESPACE_ID]?.also { nodeID ->
        require(nodeID.isValidNamespaceID()) { Messages.INVALID_NAMESPACE_ID }
    }
}

fun Input.getConfigIDFromQueryParam() : String? = this.queryStringParameters?.let { map ->
    map[Constants.CONFIG_ID]?.also { configID ->
        require(configID.isValidConfigID()) { Messages.INVALID_CONFIG_ID }
    }
}

fun Input.getBooleanFromQueryParam(queryParam : String) : Boolean  =  this.queryStringParameters?.let { map ->
    map[queryParam]?.let {
        it.toBoolean()
    } ?: false
} ?: false


fun Input.getWorkspaceIDFromPathParam(): String = this.pathParameters!!.id!!.let { id ->
    require(id.isValidWorkspaceID()) { Messages.INVALID_WORKSPACE_ID }
    id
}

fun Input.getCaptureIDFromPathParam(): String = this.pathParameters!!.id!!.let { id ->
    require(id.isValidCaptureID()) { Messages.INVALID_CAPTURE_ID }
    id
}

fun Input.getNamespaceIDFromPathParam(): String = this.pathParameters!!.namespaceID!!.let { namespaceID ->
    require(namespaceID.isValidNamespaceID()) { Messages.INVALID_NAMESPACE_ID }
    namespaceID
}

fun Input.getNodePathFromPathParam(): List<String> = this.pathParameters!!.path!!.let { nodePath ->
    val listOfNodeNames = convertToListOfNames(nodePath)
    require(listOfNodeNames.none { title -> !title.isValidTitle() }) {
        Messages.INVALID_TITLES
    }
    listOfNodeNames
}

private fun convertToListOfNames(nodePath: String): List<String> {
    return NodeHelper.getNamePath(nodePath).getListFromPath(",")
}