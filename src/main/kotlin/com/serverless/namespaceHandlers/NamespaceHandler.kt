package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.transformers.NamespaceTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager

class NamespaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val namespaceService = NamespaceService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val wdInput : Input = Input.fromMap(input)

        val strategy = NamespaceStrategyFactory.getNamespaceStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }
        return strategy.apply(wdInput, namespaceService)
    }

    companion object {
        private val LOG = LogManager.getLogger(NamespaceHandler::class.java)
    }
}
