package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class UpdateSharedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.payload?.let {
            nodeService.updateSharedNode(it, input.tokenBody.userID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_NODE)
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

    }
}