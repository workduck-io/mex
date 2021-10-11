package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.models.Namespace
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager
import java.util.*

class GetWorkspaceData : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val workspaceService = WorkspaceService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?

		val workspaceIDList: List<String> = (pathParameters!!["ids"] as String).split(",")
		val workspaces : MutableMap<String, Workspace?>? = workspaceService.getWorkspaceData(workspaceIDList)


		if (workspaces != null) {
			val responseBody = StandardResponse(workspaces.toString())
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error getting workspaces!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetWorkspace::class.java)
	}
}