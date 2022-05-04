package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class MakeNodePublicStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val nodeID = input.pathParameters?.id

        return if(nodeID != null) {
            nodeService.makeNodePublic(nodeID, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(nodeID, Messages.ERROR_MAKING_NODE_PUBLIC)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_MAKING_NODE_PUBLIC)
        }
    }
}
