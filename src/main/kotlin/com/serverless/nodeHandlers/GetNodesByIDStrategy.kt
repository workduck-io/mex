package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.workduck.service.NodeService

class GetNodesByIDStrategy: NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        return input.payload?.let { nodeIDRequest ->
            nodeService.getNodesInBatch(nodeIDRequest, input.headers.workspaceID).map {
                NodeHelper.convertNodeToNodeResponse(it)
            }.let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NODE)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_GETTING_NODE)


    }
}