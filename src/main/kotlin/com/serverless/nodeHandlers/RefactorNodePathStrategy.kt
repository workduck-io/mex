package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class RefactorNodePathStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node path"

        return input.payload?.let{ request ->
            ApiResponseHelper.generateStandardResponse(nodeService.refactor(request, input.headers.workspaceID)
                    , errorMessage)} ?: ApiResponseHelper.generateStandardErrorResponse(errorMessage)

    }
}