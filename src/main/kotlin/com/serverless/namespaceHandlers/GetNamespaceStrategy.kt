package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class GetNamespaceStrategy : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService, transformer: Transformer<Namespace>): ApiGatewayResponse {
        val errorMessage = "Error getting namespace"

        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val namespaceID = pathParameters.getOrDefault("id", "")

            val namespace: Entity? = namespaceService.getNamespace(namespaceID)
            val namespaceResponse = transformer.transform(namespace as Namespace)
            ApiResponseHelper.generateStandardResponse(namespaceResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
