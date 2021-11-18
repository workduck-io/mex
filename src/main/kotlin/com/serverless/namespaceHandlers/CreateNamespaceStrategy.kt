package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class CreateNamespaceStrategy(
        val namespaceTransformer: Transformer<Namespace>
) : NamespaceStrategy {

    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error creating namespace"

        val json = input["body"] as String
        val namespace: Entity? = namespaceService.createNamespace(json)

        val namespaceResponse: Response?  = namespaceTransformer.transform(namespace as Namespace?)
        return ApiResponseHelper.generateStandardResponse(namespaceResponse, errorMessage)
    }
}
