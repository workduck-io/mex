package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class BulkCreateNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node path"

    return input.payload?.let{ request ->
        ApiResponseHelper.generateStandardResponse(nodeService.bulkCreateNodes(request, input.headers.workspaceID, input.tokenBody.userID)
                , errorMessage)} ?: ApiResponseHelper.generateStandardErrorResponse(errorMessage)

    }
}