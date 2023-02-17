package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.isValidNamespaceID
import com.workduck.service.NodeService

class UpdateNodeBlockStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        val namespaceID = input.queryStringParameters?.let{
            it["namespaceID"]?.let{ namespaceID ->
                require(namespaceID.isValidNamespaceID()) { Messages.INVALID_NAMESPACE_ID }
                namespaceID
            }
        }

        return input.pathParameters!!.id!!.let { nodeID ->
            input.payload?.let { elementListRequest ->
                nodeService.updateNodeBlock(nodeID, input.tokenBody.userID, input.tokenBody.userID, namespaceID, elementListRequest)
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_NODE_BLOCK)
            } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST)
        }

    }
}
