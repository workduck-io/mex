package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.WorkspaceService

class GetAllWorkspacesForUserStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        return workspaceService.getAllWorkspacesForUser(input.tokenBody.username, input.tokenBody.userPoolID).let { workspaces ->
            ApiResponseHelper.generateStandardResponse(workspaces, Messages.ERROR_GETTING_WORKSPACES)
        }
    }
}
