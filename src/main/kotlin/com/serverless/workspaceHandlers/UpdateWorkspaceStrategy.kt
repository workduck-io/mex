package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.serverless.transformers.WorkspaceTransformer
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class UpdateWorkspaceStrategy(
        val namespaceTransformer : Transformer<Workspace>
) : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error updating workspace"
        val json = input["body"] as String

        val workspace: Entity? = workspaceService.updateWorkspace(json)
        val workspaceResponse: Response?  = namespaceTransformer.transform(workspace as Workspace?)

        return ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)
    }
}
