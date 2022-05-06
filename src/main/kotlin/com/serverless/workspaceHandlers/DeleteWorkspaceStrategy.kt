package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.IdentifierHelper
import com.serverless.utils.Messages
import com.workduck.service.WorkspaceService

class DeleteWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse =
            workspaceService.deleteWorkspace(input.headers.workspaceID)?.let { identifier ->
                IdentifierHelper.convertIdentifierToIdentifierResponse(identifier) }?.let { identifierResponse ->
                    ApiResponseHelper.generateStandardResponse(identifierResponse, Messages.ERROR_DELETING_WORKSPACE)
                } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_DELETING_WORKSPACE)
}
