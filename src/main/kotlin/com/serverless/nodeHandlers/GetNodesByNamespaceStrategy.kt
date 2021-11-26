package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.workduck.models.Node
import com.workduck.service.NodeService

class GetNodesByNamespaceStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting users!"

        val namespaceID = input.pathParameters?.namespaceID
        val workspaceID = input.pathParameters?.workspaceID

        return if (workspaceID != null && namespaceID != null) {
            val nodes: MutableList<String>? = nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceID)

            ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
