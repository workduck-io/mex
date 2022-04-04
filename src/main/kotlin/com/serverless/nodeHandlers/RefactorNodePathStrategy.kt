package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.models.Workspace
import com.workduck.service.NodeService
import com.workduck.service.WorkspaceService

class RefactorNodePathStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node path"

        val workspace = WorkspaceService().getWorkspace(input.headers.workspaceID) as Workspace?
                ?: throw IllegalArgumentException("Invalid Workspace ID")


        return input.payload?.let{ request ->
            ApiResponseHelper.generateStandardResponse(nodeService.refactor(request, workspace)
                    , errorMessage)} ?: ApiResponseHelper.generateStandardErrorResponse(errorMessage)

    }
}