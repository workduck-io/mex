package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.WorkspaceService

class RefreshHierarchyStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error refreshing node hierarchy of workspace"

        workspaceService.refreshNodeHierarchyForWorkspace(input.headers.workspaceID)
        return ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
    }

}
