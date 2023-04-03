package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.serverless.utils.extensions.withNotFoundException
import com.workduck.service.NodeService

class GetPublicNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { nodeID ->
            return nodeService.getPublicNode(nodeID).withNotFoundException().let {
                ApiResponseHelper.generateStandardResponse(NodeHelper.convertNodeToNodeResponse(it), Messages.ERROR_GETTING_SNIPPET)
            }
        }!! /* id cannot be empty since path has been matched */
    }
}
