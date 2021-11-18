package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class GetWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService, transformer: Transformer<Workspace>): ApiGatewayResponse {
        val errorMessage = "Error getting workspace"

        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val workspaceID = pathParameters.getOrDefault("id", "")

            val workspace: Entity? = workspaceService.getWorkspace(workspaceID)

            val workspaceResponse = transformer.transform(workspace as Workspace)

            ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
