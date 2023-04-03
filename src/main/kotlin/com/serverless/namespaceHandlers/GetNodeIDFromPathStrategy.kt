package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getListFromPath
import com.serverless.utils.extensions.getNamespaceIDFromPathParam
import com.serverless.utils.extensions.getNodeIDFromQueryParam
import com.serverless.utils.extensions.getNodePathFromPathParam
import com.serverless.utils.extensions.isValidNamespaceID
import com.serverless.utils.extensions.isValidNodeID
import com.serverless.utils.extensions.isValidTitle
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
}
