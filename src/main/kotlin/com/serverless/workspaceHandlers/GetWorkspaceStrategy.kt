package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.WorkspaceHelper
import com.workduck.models.Entity
import com.workduck.service.WorkspaceService

class GetWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error getting workspace"

        val workspace: Entity? = workspaceService.getWorkspace(input.headers.workspaceID)

        val workspaceResponse : Response? = WorkspaceHelper.convertWorkspaceToWorkspaceResponse(workspace)
        return ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)

    }
}
