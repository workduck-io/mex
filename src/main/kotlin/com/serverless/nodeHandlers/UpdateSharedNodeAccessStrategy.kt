package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class UpdateSharedNodeAccessStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating access"

        return input.pathParameters?.nodeID?.let { nodeID ->
            nodeService.changeAccessType(nodeID, input.headers.workspaceID, input.pathParameters.userID!!, input.pathParameters.access!!).let {
                ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
            }
        }!!

    }
}