package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class GetNamespaceDataStrategy(
        val namespaceTransformer: Transformer<Namespace>
) : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error getting namespaces!"
        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val namespaceIDList : List<String> = pathParameters.getOrDefault("id", "").split(",")
            val namespaces: MutableMap<String, Namespace?>? = namespaceService.getNamespaceData(namespaceIDList)

            val workspaceResponseMap = namespaces?.mapValues {
                namespaceTransformer.transform(it.value)
            }

            ApiResponseHelper.generateStandardResponse(workspaceResponseMap, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
