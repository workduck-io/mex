package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class GetWorkspaceDataStrategy : WorkspaceStrategy {
    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error getting workspaces!"
        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val workspaceIDList: List<String> = pathParameters.getOrDefault("id", "").split(",")
            val workspaces: MutableMap<String, Workspace?>? = workspaceService.getWorkspaceData(workspaceIDList)

            ApiResponseHelper.generateStandardResponse(workspaces as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
