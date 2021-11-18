package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.transformers.NodeTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Node
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager

class NodeHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val nodeService = NodeService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val routeKey = input["routeKey"] as String

        val strategy = NodeStrategyFactory.getNodeStrategy(routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }

        val transformer : Transformer<Node> = NodeTransformer()
        return strategy.apply(input, nodeService, transformer)
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeHandler::class.java)
    }
}
