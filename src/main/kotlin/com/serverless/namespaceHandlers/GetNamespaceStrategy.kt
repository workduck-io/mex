package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.serverless.utils.withNotFoundException
import com.workduck.models.Entity
import com.workduck.service.NamespaceService


class GetNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val namespaceID = input.pathParameters?.namespaceID

        return if (namespaceID != null) {
            val namespace: Entity = namespaceService.getNamespace(namespaceID).withNotFoundException()
            val namespaceResponse: Response?  = NamespaceHelper.convertNamespaceToNamespaceResponse(namespace)
            ApiResponseHelper.generateStandardResponse(namespaceResponse, Messages.ERROR_GETTING_NAMESPACE)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_GETTING_NAMESPACE)
        }
    }
}
