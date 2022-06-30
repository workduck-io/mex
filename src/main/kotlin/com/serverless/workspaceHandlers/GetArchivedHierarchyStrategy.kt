package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.WorkspaceService

class GetArchivedHierarchyStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        return ApiResponseHelper.generateStandardResponse(workspaceService.getArchivedNodeHierarchyOfWorkspace(input.headers.workspaceID), Messages.ERROR_GETTING_NODES)
    }
}
