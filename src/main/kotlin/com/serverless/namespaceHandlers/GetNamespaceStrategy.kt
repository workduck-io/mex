package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.serverless.utils.withNotFoundException
import com.workduck.service.NamespaceService


class GetNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {

        return input.pathParameters?.id?.let { namespaceID ->
            namespaceService.getNamespace(namespaceID, input.headers.workspaceID).withNotFoundException().let {
                ApiResponseHelper.generateStandardResponse(NamespaceHelper.convertNamespaceToNamespaceResponse(it), Messages.ERROR_GETTING_NAMESPACE)
            }
        }!! // id cannot be null since path was matched.

    }
}
