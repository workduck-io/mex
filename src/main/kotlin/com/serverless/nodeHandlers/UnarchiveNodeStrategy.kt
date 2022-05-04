package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class UnarchiveNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        val nodeIDRequest = input.payload

        return if(nodeIDRequest != null) {
            val nodeIDList = nodeService.unarchiveNodes(nodeIDRequest, input.headers.workspaceID)
            return ApiResponseHelper.generateStandardResponse(nodeIDList, Messages.ERROR_UNARCHIVING_NODE)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_UNARCHIVING_NODE)
        }
    }
}

