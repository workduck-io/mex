package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Response
import com.serverless.transformers.IdentifierTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class DeleteNamespaceStrategy(
        val identifierTransformer : Transformer<Identifier>
) : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error deleting namespace"

        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val namespaceID = pathParameters.getOrDefault("id", "")

            val identifier: Identifier? = namespaceService.deleteNamespace(namespaceID)

            val identifierResponse: Response?  = identifierTransformer.transform(identifier)

            ApiResponseHelper.generateStandardResponse(identifierResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
