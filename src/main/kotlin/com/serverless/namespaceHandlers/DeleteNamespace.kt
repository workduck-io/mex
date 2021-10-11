package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.models.Identifier
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager
import java.util.*


class DeleteNamespace:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val namespaceService = NamespaceService()
	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val namespaceID = pathParameters!!["id"] as String

		val identifier : Identifier? = namespaceService.deleteNamespace(namespaceID)

		if (identifier != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = identifier
			}
		}
		else{
			val responseBody = StandardResponse("Error deleting namespace!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(DeleteNamespace::class.java)
	}
}