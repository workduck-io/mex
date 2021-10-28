package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NamespaceService

class GetNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error getting namespace"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val namespaceID = pathParameters!!["id"] as String

        val namespace: Entity? = namespaceService.getNamespace(namespaceID)
        return ApiResponseHelper.generateStandardResponse(namespace as Any?, errorMessage)
    }
}
