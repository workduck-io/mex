package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class CreateNamespaceStrategy : NamespaceStrategy {

    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService, transformer: Transformer<Namespace>): ApiGatewayResponse {
        val errorMessage = "Error creating namespace"

        val json = input["body"] as String
        val namespace: Entity? = namespaceService.createNamespace(json)

        val namespaceResponse = transformer.transform(namespace as Namespace)
        return ApiResponseHelper.generateStandardResponse(namespaceResponse, errorMessage)
    }
}
