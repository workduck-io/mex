package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.WorkspaceService

class CreateWorkspaceStrategy : WorkspaceStrategy {

    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error creating workspace"

        val json = input["body"] as String
        val workspace: Entity? = workspaceService.createWorkspace(json)

        return ApiResponseHelper.generateStandardResponse(workspace as Any?, errorMessage)
    }
}
