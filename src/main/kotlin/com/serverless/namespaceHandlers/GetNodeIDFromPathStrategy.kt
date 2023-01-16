package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.getListFromPath
import com.serverless.utils.isValidNamespaceID
import com.serverless.utils.isValidNodeID
import com.serverless.utils.isValidTitle
import com.workduck.service.NamespaceService
import com.workduck.utils.NodeHelper.getNamePath

class GetNodeIDFromPathStrategy : NamespaceStrategy {

    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val namespaceID: String = input.getNamespaceIDFromPathParam()
        val nodeNameList: List<String> = input.getNodePathFromPathParam()

        val rootNodeID: String? = input.getNodeIDFromQueryParam()

        return namespaceService.getNodeIDFromPath(
            rootNodeID, namespaceID, nodeNameList,
            input.tokenBody.userID, input.headers.workspaceID
        ).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NODE)
        }
    } /* id cannot be null since path has been matched */

    // TODO ( separate as a generic extension )
    private fun Input.getNamespaceIDFromPathParam(): String = this.pathParameters!!.namespaceID!!.let { namespaceID ->
        require(namespaceID.isValidNamespaceID()) { Messages.INVALID_NAMESPACE_ID }
        namespaceID
    }

    private fun Input.getNodePathFromPathParam(): List<String> = this.pathParameters!!.path!!.let { nodePath ->
        val listOfNodeNames = convertToListOfNames(nodePath)
        require(listOfNodeNames.none { title -> !title.isValidTitle() }) {
            Messages.INVALID_TITLES
        }
        listOfNodeNames
    }

    private fun convertToListOfNames(nodePath: String): List<String> {
        return getNamePath(nodePath).getListFromPath(",")
    }

    private fun Input.getNodeIDFromQueryParam(): String? = this.queryStringParameters?.let { map ->
        map["nodeID"]?.also { nodeID ->
            require(nodeID.isValidNodeID()) { Messages.INVALID_NODE_ID }
        }
    }
}
