package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class GetAccessDataForUserStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return input.pathParameters?.id?.let {
            nodeService.getAccessDataForUser(it, input.tokenBody.userID, input.headers.workspaceID).let { access ->
                ApiResponseHelper.generateStandardResponse(access, Messages.ERROR_GETTING_RECORDS)
            }
        } ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)
    }
}