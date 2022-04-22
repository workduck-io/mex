package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class ShareNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error sharing node"

        return input.payload?.let {
            nodeService.shareNode(it, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

    }
}
