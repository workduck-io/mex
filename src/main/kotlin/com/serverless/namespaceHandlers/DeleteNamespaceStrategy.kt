package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Identifier
import com.workduck.service.NamespaceService

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error deleting namespace"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val namespaceID = pathParameters!!["id"] as String

        val identifier: Identifier? = namespaceService.deleteNamespace(namespaceID)
        return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
    }
}
