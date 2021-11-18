package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class UpdateWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService, transformer: Transformer<Workspace>): ApiGatewayResponse {
        val errorMessage = "Error updating workspace"
        val json = input["body"] as String

        val workspace: Entity? = workspaceService.updateWorkspace(json)
        val workspaceResponse = transformer.transform(workspace as Workspace)

        return ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)
    }
}
