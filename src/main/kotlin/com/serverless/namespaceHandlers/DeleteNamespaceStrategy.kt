package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Identifier
import com.workduck.service.NamespaceService

class DeleteNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error deleting namespace"

        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if (pathParameters != null) {
            val namespaceID = pathParameters["id"] as String

            val identifier: Identifier? = namespaceService.deleteNamespace(namespaceID)
            ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
