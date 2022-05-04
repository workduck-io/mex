package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.workduck.models.Node
import com.workduck.service.NodeService

class GetPublicNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val nodeID = input.pathParameters?.id

        return if(nodeID != null) {
            val node: Node? = nodeService.getPublicNode(nodeID, input.headers.workspaceID)

            val nodeResponse = NodeHelper.convertNodeToNodeResponse(node)
            ApiResponseHelper.generateStandardResponse(nodeResponse, Messages.NODE_NOT_AVAILABLE)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.NODE_NOT_AVAILABLE)
        }
    }
}
