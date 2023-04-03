package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.Messages
import com.serverless.utils.WorkspaceHelper
import com.serverless.utils.extensions.withNotFoundException
import com.workduck.models.Entity
import com.workduck.service.WorkspaceService

class GetWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val workspace: Entity = workspaceService.getWorkspace(input.headers.workspaceID).withNotFoundException()
        val workspaceResponse : Response? = WorkspaceHelper.convertWorkspaceToWorkspaceResponse(workspace)
        return ApiResponseHelper.generateStandardResponse(workspaceResponse, Messages.ERROR_GETTING_WORKSPACE)

    }
}
