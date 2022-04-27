package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class GetAllArchivedNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val workspaceID = input.pathParameters?.id

        return if(workspaceID != null) {
            val nodeIDList: MutableList<String>? = nodeService.getAllArchivedSnippetIDsOfWorkspace(workspaceID)
            ApiResponseHelper.generateStandardResponse(nodeIDList as Any?, Messages.ERROR_GETTING_NODES)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_GETTING_NODES)
        }
    }
}
