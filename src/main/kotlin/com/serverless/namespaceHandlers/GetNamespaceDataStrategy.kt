package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class GetNamespaceDataStrategy : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService, transformer: Transformer<Namespace>): ApiGatewayResponse {
        val errorMessage = "Error getting namespaces!"
        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val namespaceIDList : List<String> = pathParameters.getOrDefault("id", "").split(",")
            val namespaces: MutableMap<String, Namespace?>? = namespaceService.getNamespaceData(namespaceIDList)

            val workspaceResponseMap = namespaces?.mapValues {
                transformer.transform(it.value)
            }

            ApiResponseHelper.generateStandardResponse(workspaceResponseMap, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
