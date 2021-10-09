package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.serverless.workspaceHandlers.CreateWorkspace
import com.workduck.service.UserService
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.util.*

class CreateUser: RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userService = UserService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {

		val json = input["body"] as String
		println("BODY STARTS" + json + "BODY ENDS")

		val user = userService.createUser(json)

		if (user != null) {
			val responseBody = StandardResponse(user)
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error creating user!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(CreateUser::class.java)
	}
}