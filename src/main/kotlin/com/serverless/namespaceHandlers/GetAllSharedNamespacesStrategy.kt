package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class GetAllSharedNamespacesStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        return namespaceService.getAllSharedNamespacesWithUser(input.tokenBody.userID).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NODES)
        }
    }
}