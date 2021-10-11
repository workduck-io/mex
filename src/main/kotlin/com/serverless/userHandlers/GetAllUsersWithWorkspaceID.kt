package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.NodeService
import com.workduck.service.UserService
import org.apache.logging.log4j.LogManager
import java.util.*

class GetAllUsersWithWorkspaceID: RequestHandler<Map<String, Any>, ApiGatewayResponse> {
	private val userService = UserService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val workspaceID = pathParameters!!["id"] as String

		val users : MutableList<String>? = userService.getAllUsersWithWorkspaceID(workspaceID)


		if (users != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = users
			}
		}
		else{
			val responseBody = StandardResponse("Error getting users!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetAllUsersWithNamespaceID::class.java)
	}
}