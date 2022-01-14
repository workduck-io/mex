package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class MoveBlockStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error moving block"

        val blockID = input.pathParameters?.blockID
        val nodeID1 = input.pathParameters?.nodeID1
        val nodeID2 = input.pathParameters?.nodeID2

        if(blockID != null && nodeID1 != null && nodeID2 != null){
            nodeService.moveBlock(blockID, nodeID1, nodeID2)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid Parameters", 400)
        }
        return ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)

    }


}