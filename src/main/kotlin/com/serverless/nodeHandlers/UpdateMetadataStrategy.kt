package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidNodeID
import com.workduck.service.NodeService

class UpdateMetadataStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val nodeID = input.pathParameters!!.id!!.let { id ->
            require(id.isValidNodeID()) { Messages.INVALID_NODE_ID }
            id
        }
        return input.payload?.let {
            nodeService.updateMetadataOfNode(it, nodeID, input.headers.workspaceID, input.tokenBody.userID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_NODE_METADATA)
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST)
    }
}