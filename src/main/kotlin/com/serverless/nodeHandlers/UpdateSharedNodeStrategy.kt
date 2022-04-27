package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.workduck.service.NodeService

class UpdateSharedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.payload?.let {
            nodeService.updateSharedNode(it, input.tokenBody.userID).let { node ->
                ApiResponseHelper.generateStandardResponse(NodeHelper.convertNodeToNodeResponse(node), Messages.ERROR_UPDATING_ACCESS)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

    }
}