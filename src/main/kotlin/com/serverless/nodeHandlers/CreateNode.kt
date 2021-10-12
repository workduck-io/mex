package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*


class CreateNode:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val nodeService = NodeService()

    override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {

		val json = input["body"] as String
		//println("BODY STARTS" + json + "BODY ENDS")

		val node : Entity? = nodeService.createNode(json)

		if (node != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = node
			}
		}
		else{
			val responseBody = StandardResponse("Error creating node!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
    companion object {
        private val LOG = LogManager.getLogger(CreateNode::class.java)
    }
}