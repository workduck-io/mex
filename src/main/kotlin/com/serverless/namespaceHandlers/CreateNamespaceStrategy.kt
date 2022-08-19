package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.workduck.service.NamespaceService

class CreateNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {

        return input.payload?.let { namespaceRequest ->
            namespaceService.createNamespace(namespaceRequest, input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(NamespaceHelper.convertNamespaceToNamespaceResponse(it), Messages.ERROR_CREATING_NAMESPACE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }
}
