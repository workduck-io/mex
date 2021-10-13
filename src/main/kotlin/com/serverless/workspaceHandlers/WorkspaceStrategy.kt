package com.serverless.workspaceHandlers

import com.serverless.ApiGatewayResponse
import com.workduck.service.NamespaceService
import com.workduck.service.WorkspaceService

interface WorkspaceStrategy {
	fun apply(input: Map<String, Any>, workspaceService: WorkspaceService) : ApiGatewayResponse
}