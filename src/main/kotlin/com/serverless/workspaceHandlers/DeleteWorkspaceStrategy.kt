package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.IdentifierHelper
import com.workduck.service.WorkspaceService

class DeleteWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse =
            workspaceService.deleteWorkspace(input.headers.workspaceID)?.let { identifier ->
                IdentifierHelper.convertIdentifierToIdentifierResponse(identifier) }?.let { identifierResponse ->
                    ApiResponseHelper.generateStandardResponse(identifierResponse, "Error deleting workspace")
                } ?: ApiResponseHelper.generateStandardErrorResponse("Error deleting workspace")
}
