package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.WorkspaceHelper
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class GetWorkspaceDataStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val workspaceIDs = input.pathParameters?.ids

        return if (workspaceIDs != null) {
            val workspaceIDList: List<String> = workspaceIDs.split(",")
            val workspaces: MutableMap<String, Workspace?> = workspaceService.getWorkspaceData(workspaceIDList)

            val workspaceResponseMap = workspaces.mapValues {
                WorkspaceHelper.convertWorkspaceToWorkspaceResponse(it.value)
            }

            ApiResponseHelper.generateStandardResponse(workspaceResponseMap as Any, Messages.ERROR_GETTING_WORKSPACES)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_GETTING_WORKSPACES)
        }
    }
}
