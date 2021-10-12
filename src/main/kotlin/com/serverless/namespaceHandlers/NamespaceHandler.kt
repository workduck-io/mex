package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Namespace
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager

class NamespaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val namespaceService = NamespaceService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val path = input["path"] as String

		if (method == "GET" && path.startsWith("/namespace/NAMESPACE")) {

			val errorMessage = "Error getting namespace"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val namespaceID = pathParameters!!["id"] as String

			val namespace : Entity? = namespaceService.getNamespace(namespaceID)
			return ApiResponseHelper.generateStandardResponse(namespace as Any?, errorMessage)

		} else if (method == "POST" && path == "/namespace") {

			val errorMessage = "Error creating namespace"

			val json = input["body"] as String
			val namespace : Entity? = namespaceService.createNamespace(json)

			return ApiResponseHelper.generateStandardResponse(namespace as Any?, errorMessage)

		} else if (method == "DELETE" && path.startsWith("/namespace/NAMESPACE")) {

			val errorMessage = "Error deleting namespace"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val namespaceID = pathParameters!!["id"] as String

			val identifier : Identifier? = namespaceService.deleteNamespace(namespaceID)
			return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)

		} else if (method == "POST" && path == "/namespace/update") {
			val errorMessage = "Error updating namespace"
			val json = input["body"] as String

			val namespace : Entity? = namespaceService.updateNamespace(json)
			return ApiResponseHelper.generateStandardResponse(namespace as Any?, errorMessage)

		} else if (method == "GET" && path.startsWith("/namespace/data/") ) {

			val errorMessage = "Error getting namespaces!"
			val pathParameters = input["pathParameters"] as Map<*, *>?

			val namespaceIDList: List<String> = (pathParameters!!["ids"] as String).split(",")
			val namespaces : MutableMap<String, Namespace?>? = namespaceService.getNamespaceData(namespaceIDList)

			return ApiResponseHelper.generateStandardResponse(namespaces as Any?, errorMessage)
		} else {
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}
		}

	}

	companion object {
		private val LOG = LogManager.getLogger(NamespaceHandler::class.java)
	}
}