package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.WorkspaceService

class GetWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error getting workspace"

        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if (pathParameters != null) {
            val workspaceID = pathParameters["id"] as String

            val workspace: Entity? = workspaceService.getWorkspace(workspaceID)
            ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
