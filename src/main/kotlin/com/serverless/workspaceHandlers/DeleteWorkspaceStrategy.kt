package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class DeleteWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService, transformer: Transformer<Workspace>): ApiGatewayResponse {
        val errorMessage = "Error deleting workspace"

        val pathParameters = input["pathParameters"] as Map<String, String>?
        return if (pathParameters != null) {
            val workspaceID = pathParameters.getOrDefault("id", "")

            val identifier: Identifier? = workspaceService.deleteWorkspace(workspaceID)
            ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
