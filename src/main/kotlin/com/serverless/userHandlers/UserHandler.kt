package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.nodeHandlers.NodeStrategyFactory
import com.workduck.service.UserService
import org.apache.logging.log4j.LogManager

class UserHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userService = UserService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val routeKey = input["routeKey"] as String

		val strategy = UserStrategyFactory.getUserStrategy(routeKey)

		if (strategy == null ){
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}

		}
		return strategy.apply(input, userService)

	}

	companion object {
		private val LOG = LogManager.getLogger(UserHandler::class.java)
	}
}