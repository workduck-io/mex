package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class GetAccessDataForUserStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {

        return input.pathParameters?.id?.let {
            namespaceService.getAccessDataForUser(it, input.tokenBody.userID, input.headers.workspaceID).let { access ->
                ApiResponseHelper.generateStandardResponse(access, Messages.ERROR_GETTING_RECORDS)
            }
        } ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)
    }
}