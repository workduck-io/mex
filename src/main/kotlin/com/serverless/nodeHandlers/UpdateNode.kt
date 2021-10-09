package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*

class UpdateNode:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val nodeService = NodeService()

    override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {

        val json = input["body"] as String

        val node = nodeService.updateNode(json)

		if (node != null) {
			val responseBody = StandardResponse(node)
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error updating node!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
    }
    companion object {
        private val LOG = LogManager.getLogger(UpdateNode::class.java)
    }
}