package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.util.*


class UpdateWorkspace:RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val workspaceService = WorkspaceService()

	override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {

		val json = input["body"] as String

		val workspace : Entity? = workspaceService.updateWorkspace(json)

		if (workspace != null) {
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = workspace
			}
		}
		else{
			val responseBody = StandardResponse("Error updating workspace!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(UpdateWorkspace::class.java)
	}
}