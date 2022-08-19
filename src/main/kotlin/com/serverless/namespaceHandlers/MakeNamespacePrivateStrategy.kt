package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class MakeNamespacePrivateStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        return input.pathParameters?.id?.let {
            namespaceService.makeNamespacePrivate(it, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_MAKING_NODE_PUBLIC)
        }!!
    }
}