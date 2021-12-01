package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class MakeNodePublicStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error making node public"

        val nodeID = input.pathParameters?.id

        return if(nodeID != null) {
            nodeService.makeNodePublic(nodeID)
            ApiResponseHelper.generateStandardResponse(nodeID, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
