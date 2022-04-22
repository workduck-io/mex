package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class ShareNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error sharing node"

        val onlyRead = input.queryStringParameters?.let{
            it["version"]?.toBoolean()
        } ?: false

        return input.pathParameters?.nodeID?.let { nodeID ->
            nodeService.shareNode(nodeID, input.tokenBody.userID, input.headers.workspaceID, input.pathParameters.userID!!, onlyRead).let {
                ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
            }
        }!!

    }
}
