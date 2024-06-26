package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.WorkspaceHelper
import com.workduck.service.WorkspaceService

class UpdateWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        return input.payload?.let { workspaceRequestObject ->
            workspaceService.updateWorkspace(workspaceRequestObject, input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_WORKSPACE)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_UPDATING_WORKSPACE)
    }
}
