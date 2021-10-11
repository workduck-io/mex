package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.models.Identifier
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.util.*


class DeleteWorkspace:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val workspaceService = WorkspaceService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val workspaceID = pathParameters!!["id"] as String

		val identifier : Identifier? = workspaceService.deleteWorkspace(workspaceID)


		if (identifier != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = identifier
			}
		}
		else{
			val responseBody = StandardResponse("Error deleting workspace!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(DeleteWorkspace::class.java)
	}
}