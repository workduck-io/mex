package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class GetNamespaceDataStrategy : NamespaceStrategy {
    override fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error getting namespaces!"
        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if (pathParameters != null) {
            val namespaceIDList: List<String> = (pathParameters["ids"] as String).split(",")
            val namespaces: MutableMap<String, Namespace?>? = namespaceService.getNamespaceData(namespaceIDList)

            ApiResponseHelper.generateStandardResponse(namespaces as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
