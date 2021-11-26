package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.serverless.utils.NamespaceHelper
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService

class GetNamespaceDataStrategy : NamespaceStrategy {
    override fun apply(input: Input, namespaceService: NamespaceService): ApiGatewayResponse {
        val errorMessage = "Error getting namespaces!"
        val namespaceIDs = input.pathParameters?.ids

        return if (namespaceIDs != null) {
            val namespaceIDList : List<String> = namespaceIDs.split(",")

            val namespaces: MutableMap<String, Namespace?>? = namespaceService.getNamespaceData(namespaceIDList)

            val namespaceResponseMap = namespaces?.mapValues {
                NamespaceHelper.convertNamespaceToNamespaceResponse(it.value)
            }

            ApiResponseHelper.generateStandardResponse(namespaceResponseMap, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
