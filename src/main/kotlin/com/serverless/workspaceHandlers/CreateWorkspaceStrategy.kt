package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.WorkspaceHelper
import com.workduck.service.WorkspaceService

class CreateWorkspaceStrategy : WorkspaceStrategy {

    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error creating workspace"

        return input.payload?.let { workspaceRequestObject ->
            workspaceService.createWorkspace(workspaceRequestObject).let { workspace ->
                ApiResponseHelper.generateStandardResponse(WorkspaceHelper.convertWorkspaceToWorkspaceResponse(workspace), errorMessage)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse(errorMessage)

    }
}
