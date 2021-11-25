package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class GetWorkspaceStrategy(
        val namespaceTransformer : Transformer<Workspace>
) : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error getting workspace"

        val workspaceID = input.pathParameters?.id

        return if (workspaceID != null) {
            val workspace: Entity? = workspaceService.getWorkspace(workspaceID)

            val workspaceResponse
            val workspaceResponse : Response? = namespaceTransformer.transform(workspace as Workspace?)

            ApiResponseHelper.generateStandardResponse(workspaceResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
