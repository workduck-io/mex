package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class GetAllSharedNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return nodeService.getAllSharedNodesWithUser(input.tokenBody.userID).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NODES)
        }
    }
}