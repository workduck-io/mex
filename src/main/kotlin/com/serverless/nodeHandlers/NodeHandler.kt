package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.models.NodeRequest
import com.serverless.models.NodeResponse
import com.serverless.transformers.NodeTransformer
import com.serverless.transformers.Transformer
import com.workduck.models.Node
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NodeHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val nodeService = NodeService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        println(input)

        val wdInput : Input = Input.fromMap(input)

        println("WD-Input :  $wdInput")


        if(wdInput.body != null) {
            val nodeResponse: NodeRequest = Helper.objectMapper.readValue(wdInput.body)
            println(nodeResponse.toString())
        }


        val routeKey = input["routeKey"] as String

        val strategy = NodeStrategyFactory.getNodeStrategy(routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }

        return strategy.apply(input, nodeService)
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeHandler::class.java)
    }
}
