package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

class DeleteWorkspaceStrategy(
        val identifierTransformer : Transformer<Identifier>
) : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error deleting workspace"

        val pathParameters = input["pathParameters"] as Map<String, String>?
        return if (pathParameters != null) {
            val workspaceID = pathParameters.getOrDefault("id", "")

            val identifier: Identifier? = workspaceService.deleteWorkspace(workspaceID)

            val identifierResponse : Response?  = identifierTransformer.transform(identifier)
            ApiResponseHelper.generateStandardResponse(identifierResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
