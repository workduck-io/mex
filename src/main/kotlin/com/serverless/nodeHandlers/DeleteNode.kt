package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*

class DeleteNode:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val nodeService = NodeService()

    override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val nodeID = pathParameters!!["id"] as String

        val id = nodeService.deleteNode(nodeID)
		if (id != null) {
			val responseBody = StandardResponse(id)
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
				headers = mapOf(
					"Access-Control-Allow-Origin" to "*",
					"Access-Control-Allow-Credentials" to  true
				)
			}
		}
		else{
			val responseBody = StandardResponse("Error deleting node!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}

    }
    companion object {
        private val LOG = LogManager.getLogger(DeleteNode::class.java)
    }
}