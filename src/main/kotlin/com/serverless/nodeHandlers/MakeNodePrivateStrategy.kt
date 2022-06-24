package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class MakeNodePrivateStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.pathParameters?.id?.let {
            nodeService.makeNodePrivate(it, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_MAKING_NODE_PRIVATE)
        }!!
    }
}
