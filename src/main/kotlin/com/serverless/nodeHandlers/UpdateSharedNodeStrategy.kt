package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.NodeHelper
import com.workduck.service.NodeService

class UpdateSharedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating access"

        return input.payload?.let {
            nodeService.updateSharedNode(it, input.tokenBody.userID).let { node ->
                ApiResponseHelper.generateStandardResponse(NodeHelper.convertNodeToNodeResponse(node), errorMessage)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

    }
}