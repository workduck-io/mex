package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class ShareNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        return input.payload?.let {
            nodeService.shareNode(it, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_SHARING_NODE)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

    }
}
