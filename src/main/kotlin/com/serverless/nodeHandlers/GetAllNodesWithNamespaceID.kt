package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*


class GetAllNodesWithNamespaceID:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val nodeService = NodeService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val namespaceID = pathParameters!!["namespaceID"] as String
		val workspaceID = pathParameters!!["workspaceID"] as String

		val nodes : MutableList<String>? = nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceID)


		if (nodes != null) {
			val responseBody = StandardResponse(nodes.toString())
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error getting nodes!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetAllNodesWithNamespaceID::class.java)
	}
}