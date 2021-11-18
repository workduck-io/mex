package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Node
import com.workduck.service.NodeService

class GetNodesByNamespaceStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService, transformer: Transformer<Node>): ApiGatewayResponse {
        val errorMessage = "Error getting users!"

        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val namespaceID = pathParameters.getOrDefault("namespaceID", "")
            val workspaceID = pathParameters.getOrDefault("workspaceID", "")

            val nodes: MutableList<String>? = nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceID)

            ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
