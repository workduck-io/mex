package com.serverless.namespaceHandlers

import com.serverless.ApiGatewayResponse
import com.workduck.service.NamespaceService

interface NamespaceStrategy {
    fun apply(input: Map<String, Any>, namespaceService: NamespaceService): ApiGatewayResponse
}
