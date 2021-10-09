package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager
import java.util.*


class CreateNamespace:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val namespaceService = NamespaceService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {

		val json = input["body"] as String

		val namespace = namespaceService.createNamespace(json)

		if (namespace != null) {
			val responseBody = StandardResponse(namespace.toString())
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error creating namespace!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(CreateNamespace::class.java)
	}
}