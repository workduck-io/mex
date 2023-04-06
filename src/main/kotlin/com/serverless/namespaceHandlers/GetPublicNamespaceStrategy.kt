package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.serverless.utils.extensions.withNotFoundException
import com.workduck.service.NamespaceService

class GetPublicNamespaceStrategy: NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { namespaceID ->
            return namespaceService.getPublicNamespace(namespaceID).withNotFoundException().let {
                ApiResponseHelper.generateStandardResponse(NamespaceHelper.convertNamespaceToNamespaceResponse(it), Messages.ERROR_GETTING_NAMESPACE)
            }
        }!! /* id cannot be empty since path has been matched */
    }
}