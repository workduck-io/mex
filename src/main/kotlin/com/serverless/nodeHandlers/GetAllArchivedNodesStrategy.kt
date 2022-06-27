package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class GetAllArchivedNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        return ApiResponseHelper.generateStandardResponse(nodeService.getAllArchivedNodeIDsOfWorkspace(input.headers.workspaceID), Messages.ERROR_GETTING_NODES)
    }
}
