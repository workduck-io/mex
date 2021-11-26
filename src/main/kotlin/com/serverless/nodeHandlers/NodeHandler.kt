package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager

class NodeHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val nodeService = NodeService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val wdInput : Input = Input.fromMap(input)

        val strategy = NodeStrategyFactory.getNodeStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }

        return strategy.apply(wdInput, nodeService)
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeHandler::class.java)
    }
}
