package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.service.UserService
import org.apache.logging.log4j.LogManager
import java.util.*


class GetUser:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userService = UserService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val userID = pathParameters!!["id"] as String

		val user : Entity? = userService.getUser(userID)


		if (user != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = user
			}
		}
		else{
			val responseBody = StandardResponse("Error getting user!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetUser::class.java)
	}
}