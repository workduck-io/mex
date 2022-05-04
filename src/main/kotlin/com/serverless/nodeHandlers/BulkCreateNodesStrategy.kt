package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class BulkCreateNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.payload?.let{ request ->
            ApiResponseHelper.generateStandardResponse(nodeService.bulkCreateNodes(request, input.headers.workspaceID, input.tokenBody.userID)
                    , Messages.ERROR_CREATING_NODE)} ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_CREATING_NODE)

        }
}