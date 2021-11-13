package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class GetNodesByWorkspaceStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting users!"
        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if (pathParameters != null) {
            val workspaceID = pathParameters["workspaceId"] as String

            val nodes: MutableList<String>? = nodeService.getAllNodesWithWorkspaceID(workspaceID)
            ApiResponseHelper.generateStandardResponse(nodes as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
