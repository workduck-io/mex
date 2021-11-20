package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class GetNodesByNamespaceStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting users!"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val namespaceID = pathParameters!!["namespaceID"] as String
        val workspaceID = pathParameters["workspaceID"] as String

        val nodes: MutableList<String>? = nodeService.getAllNodesWithNamespaceID(namespaceID, workspaceID)

        return ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
    }
}
