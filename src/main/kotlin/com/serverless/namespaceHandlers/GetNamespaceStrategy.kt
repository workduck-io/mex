package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.serverless.utils.extensions.withNotFoundException
import com.workduck.service.NamespaceService


class GetNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {

        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.getNamespace(input.headers.workspaceID, namespaceID, input.tokenBody.userID).withNotFoundException().let {
                ApiResponseHelper.generateStandardResponse(NamespaceHelper.convertNamespaceToNamespaceResponse(it), Messages.ERROR_GETTING_NAMESPACE)
            }
        }!! // id cannot be null since path was matched.

    }
}
