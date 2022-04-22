package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.NodeHelper.convertNodeToNodeResponse
import com.workduck.service.NodeService

class GetSharedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting shared node"

        return input.pathParameters?.nodeID?.let { nodeID ->
            nodeService.getSharedNode(nodeID, input.tokenBody.userID).let { node ->
                ApiResponseHelper.generateStandardResponse(convertNodeToNodeResponse(node), errorMessage)
            }
        }!! /* since path gets matched, nodeID will always be not null */

    }
}