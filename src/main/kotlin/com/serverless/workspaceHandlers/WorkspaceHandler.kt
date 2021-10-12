package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService
import org.apache.logging.log4j.LogManager

class WorkspaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val workspaceService = WorkspaceService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val path = input["path"] as String

		if (method == "GET" && path.startsWith("/workspace/WORKSPACE")) {

			val errorMessage = "Error getting workspace"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val workspaceID = pathParameters!!["id"] as String

			val workspace: Entity? = workspaceService.getWorkspace(workspaceID)
			return ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)

		} else if (method == "POST" && path == "/workspace") {

			val errorMessage = "Error creating workspace"

			val json = input["body"] as String
			val workspace: Entity? = workspaceService.createWorkspace(json)

			return ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)

		} else if (method == "DELETE" && path.startsWith("/workspace/WORKSPACE")) {

			val errorMessage = "Error deleting workspace"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val workspaceID = pathParameters!!["id"] as String

			val identifier: Identifier? = workspaceService.deleteWorkspace(workspaceID)
			return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)

		} else if (method == "POST" && path == "/workspace/update") {
			val errorMessage = "Error updating workspace"
			val json = input["body"] as String

			val workspace: Entity? = workspaceService.updateWorkspace(json)
			return ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)

		} else if (method == "GET" && path.startsWith("/workspace/data/")) {

			val errorMessage = "Error getting workspaces!"
			val pathParameters = input["pathParameters"] as Map<*, *>?

			val workspaceIDList: List<String> = (pathParameters!!["ids"] as String).split(",")
			val workspaces: MutableMap<String, Workspace?>? = workspaceService.getWorkspaceData(workspaceIDList)

			return ApiResponseHelper.generateStandardResponse(workspaces as Any?, errorMessage)
		} else {
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}
		}

	}

	companion object {
		private val LOG = LogManager.getLogger(WorkspaceHandler::class.java)
	}
}
