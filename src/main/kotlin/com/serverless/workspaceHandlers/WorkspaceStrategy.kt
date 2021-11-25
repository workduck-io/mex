package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.workduck.models.Workspace
import com.workduck.service.WorkspaceService

interface WorkspaceStrategy {
    fun apply(input: Input, workspaceService: WorkspaceService): ApiGatewayResponse
}
