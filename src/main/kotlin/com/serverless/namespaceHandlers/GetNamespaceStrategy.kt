package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService


class GetNamespaceStrategy(
        val namespaceTransformer: Transformer<Namespace>
) : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error getting namespace"

        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val namespaceID = pathParameters.getOrDefault("id", "")

            val namespace: Entity? = namespaceService.getNamespace(namespaceID)
            val namespaceResponse: Response?  = namespaceTransformer.transform(namespace as Namespace?)
            ApiResponseHelper.generateStandardResponse(namespaceResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
