package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.workduck.models.Node
import com.workduck.service.NodeService

class GetNodesByWorkspaceStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting nodes!"

        val workspaceID = input.pathParameters?.workspaceID

        return if (workspaceID != null) {
            val nodes: MutableList<String>? = nodeService.getAllNodesWithWorkspaceID(workspaceID)
            ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
