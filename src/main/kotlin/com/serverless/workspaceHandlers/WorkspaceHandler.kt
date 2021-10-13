package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.RequestObject
import com.serverless.StandardResponse
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager

class WorkspaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val workspaceService = WorkspaceService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val resource = input["resource"] as String

		val requestObject = RequestObject(method, resource)

		val strategy = WorkspaceStrategyFactory.getWorkspaceStrategy(requestObject)

		if (strategy == null ){
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}

		}
		return strategy.apply(input, workspaceService)


	}

	companion object {
		private val LOG = LogManager.getLogger(WorkspaceHandler::class.java)
	}
}
