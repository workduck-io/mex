package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class GetNodesByNamespaceStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting users!"

        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if (pathParameters != null) {
            val namespaceID = pathParameters["namespaceID"] as String
            val workspaceID = pathParameters["workspaceID"] as String

            val nodes: MutableList<String>? = nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceID)

            ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
