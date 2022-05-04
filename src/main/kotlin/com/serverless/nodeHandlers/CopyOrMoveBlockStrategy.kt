package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class CopyOrMoveBlockStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val copyOrMoveRequest = input.payload

        if(copyOrMoveRequest != null){
            nodeService.copyOrMoveBlock(copyOrMoveRequest, input.headers.workspaceID)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid Parameters", 400)
        }
        return ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_MOVING_BLOCK)

    }


}