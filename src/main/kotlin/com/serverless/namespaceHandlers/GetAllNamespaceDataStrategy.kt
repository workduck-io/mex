package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService

class GetAllNamespaceDataStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {

        return namespaceService.getAllNamespaceData(input.headers.workspaceID).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_NAMESPACES)
        }
    }
}
