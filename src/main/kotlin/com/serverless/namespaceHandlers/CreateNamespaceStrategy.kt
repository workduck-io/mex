package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.Messages
import com.serverless.utils.NamespaceHelper
import com.workduck.models.Entity
import com.workduck.service.NamespaceService

class CreateNamespaceStrategy : NamespaceStrategy {

    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val namespaceRequestObject  = input.payload
        val namespace: Entity? = namespaceService.createNamespace(namespaceRequestObject)

        val namespaceResponse: Response?  = NamespaceHelper.convertNamespaceToNamespaceResponse(namespace)
        return ApiResponseHelper.generateStandardResponse(namespaceResponse, Messages.ERROR_CREATING_NAMESPACE)
    }
}
