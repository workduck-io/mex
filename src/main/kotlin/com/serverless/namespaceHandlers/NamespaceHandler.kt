package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager

class NamespaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val namespaceService = NamespaceService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val routeKey = input["routeKey"] as String

        val strategy = NamespaceStrategyFactory.getNamespaceStrategy(routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }
        return strategy.apply(input, namespaceService)
    }

    companion object {
        private val LOG = LogManager.getLogger(NamespaceHandler::class.java)
    }
}
