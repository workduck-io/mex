package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager

class WorkspaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val workspaceService = WorkspaceService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val routeKey = input["routeKey"] as String

		val strategy = WorkspaceStrategyFactory.getWorkspaceStrategy(routeKey)

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
