package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.RequestObject
import com.serverless.StandardResponse
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager

class NamespaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val namespaceService = NamespaceService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val resource = input["resource"] as String

		val requestObject = RequestObject(method, resource)

		val strategy = NamespaceStrategyFactory.getNamespaceStrategy(requestObject)

		if (strategy == null ){
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}

		}
		return strategy.apply(input, namespaceService)

	}

	companion object {
		private val LOG = LogManager.getLogger(NamespaceHandler::class.java)
	}
}