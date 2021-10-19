package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.RequestObject
import com.serverless.StandardResponse
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager

class UserIdentifierMappingHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userIdentifierMappingService = UserIdentifierMappingService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val resource = input["resource"] as String

		val requestObject = RequestObject(method, resource)

		val strategy = UserIdentifierMappingStrategyFactory.getUserIdentifierMappingStrategy(requestObject)

		if (strategy == null ){
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}

		}
		return strategy.apply(input, userIdentifierMappingService)

	}

	companion object {
		private val LOG = LogManager.getLogger(UserIdentifierMappingHandler::class.java)
	}
}