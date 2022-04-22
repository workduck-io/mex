package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class RevokeNodeAccessStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating access"

        return input.pathParameters?.nodeID?.let { nodeID ->
            nodeService.revokeSharedAccess(nodeID, input.headers.workspaceID, input.pathParameters.userID!!).let {
                ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
            }
        }!!

    }
}