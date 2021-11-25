package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.serverless.transformers.WorkspaceTransformer
import com.serverless.utils.WorkspaceHelper
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class UpdateWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error updating workspace"

        val workspaceRequestObject = input.payload

        val workspace: Entity? = workspaceService.updateWorkspace(workspaceRequestObject)

        val workspaceResponse: Response?  = WorkspaceHelper.convertWorkspaceToWorkspaceResponse(workspace)

        return ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)
    }
}
