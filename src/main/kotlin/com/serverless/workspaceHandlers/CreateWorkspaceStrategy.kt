package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class CreateWorkspaceStrategy(
        val namespaceTransformer : Transformer<Workspace>
) : WorkspaceStrategy {

    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error creating workspace"

        val json = input["body"] as String
        val workspace: Entity? = workspaceService.createWorkspace(json)

        val workspaceResponse : Response?  = namespaceTransformer.transform(workspace as Workspace?)

        return ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)
    }
}
