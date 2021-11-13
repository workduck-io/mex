package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Identifier
import com.workduck.service.WorkspaceService

class DeleteWorkspaceStrategy : WorkspaceStrategy {
    override fun apply(input: Map<String, Any>, workspaceService: WorkspaceService): ApiGatewayResponse {
        val errorMessage = "Error deleting workspace"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        return if (pathParameters != null) {
            val workspaceID = pathParameters["id"] as String

            val identifier: Identifier? = workspaceService.deleteWorkspace(workspaceID)
            ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
