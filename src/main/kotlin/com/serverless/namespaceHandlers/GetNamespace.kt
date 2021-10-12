package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager
import java.util.*



class GetNamespace:RequestHandler<Map<String, Any>, ApiGatewayResponse> {
	private val namespaceService = NamespaceService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val namespaceID = pathParameters!!["id"] as String

		val namespace : Entity? = namespaceService.getNamespace(namespaceID)

		if (namespace != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = namespace
			}
		}
		else{
			val responseBody = StandardResponse("Error getting namespace!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetNamespace::class.java)
	}
}