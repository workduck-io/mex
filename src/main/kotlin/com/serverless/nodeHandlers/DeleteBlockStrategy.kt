package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class DeleteBlockStrategy: NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return  input.pathParameters!!.id!!.let { nodeID ->
            input.payload?.let { blockIDsRequest ->
                nodeService.deleteBlockFromNode(blockIDsRequest, input.headers.workspaceID, nodeID, input.tokenBody.userID)

                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_BLOCK)
            } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_DELETING_BLOCK)
        }
    }
}