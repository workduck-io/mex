package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.WorkspaceService

class GetHierarchyStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error getting node hierarchy for workspace"

        val nodeHierarchy: List<String> = workspaceService.getNodeHierarchyOfWorkspace(input.headers.workspaceID)

        return ApiResponseHelper.generateStandardResponse(nodeHierarchy, errorMessage)

    }
}