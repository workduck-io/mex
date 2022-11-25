package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class CopyOrMoveBlockStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.payload?.let {
            nodeService.copyOrMoveBlock(it, input.headers.workspaceID, input.tokenBody.userID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_MOVING_BLOCK)
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }
}


