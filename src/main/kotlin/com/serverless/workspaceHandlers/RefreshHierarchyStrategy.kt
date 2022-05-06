package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.WorkspaceService

class RefreshHierarchyStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        workspaceService.refreshNodeHierarchyForWorkspace(input.headers.workspaceID)
        return ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_REFRESHING_HIERARCHY)
    }

}
