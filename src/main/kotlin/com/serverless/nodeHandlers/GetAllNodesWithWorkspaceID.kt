package com.serverless.nodeHandlers

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*


class GetAllNodesWithWorkspaceID:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val nodeService = NodeService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val workspaceID = pathParameters!!["id"] as String

		val nodes : MutableList<String>? = nodeService.getAllNodesWithWorkspaceID(workspaceID)


		if (nodes != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = nodes
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
		private val LOG = LogManager.getLogger(GetAllNodesWithWorkspaceID::class.java)
	}
}