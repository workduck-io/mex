package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class GetAllSharedUsersStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting shared node"

        return input.pathParameters?.id?.let { nodeID ->
            nodeService.getAllSharedUsersOfNode(nodeID, input.tokenBody.userID, input.headers.workspaceID).let { userIDToAccessMap ->
                ApiResponseHelper.generateStandardResponse(userIDToAccessMap, errorMessage)
            }
        }!! /* since path gets matched, nodeID will always be not null */

    }
}