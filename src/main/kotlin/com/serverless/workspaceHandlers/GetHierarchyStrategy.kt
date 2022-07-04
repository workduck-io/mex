package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.WorkspaceService

class GetHierarchyStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val getMetadata = input.queryStringParameters?.let {
            it["getMetadata"]?.toBoolean()
        } ?: false

        return when (getMetadata) {
            true -> ApiResponseHelper.generateStandardResponse(workspaceService.getNodeHierarchyOfWorkspaceWithMetaData(input.headers.workspaceID), Messages.ERROR_GETTING_HIERARCHY)
            false -> ApiResponseHelper.generateStandardResponse(workspaceService.getNodeHierarchyOfWorkspace(input.headers.workspaceID), Messages.ERROR_GETTING_HIERARCHY)

        }

    }
}