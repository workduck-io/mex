package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.IdentifierHelper
import com.workduck.models.Identifier
import com.workduck.service.WorkspaceService

class DeleteWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error deleting workspace"

        val identifier: Identifier? = workspaceService.deleteWorkspace(input.headers.workspaceID)
        val identifierResponse : Response?  = IdentifierHelper.convertIdentifierToIdentifierResponse(identifier)
        return ApiResponseHelper.generateStandardResponse(identifierResponse, errorMessage)

    }
}
