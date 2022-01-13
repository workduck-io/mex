package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.workduck.models.Entity
import com.workduck.service.NodeService

class UnarchiveNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error un-archiving node"

        val nodeIDRequest = input.payload

        return if(nodeIDRequest != null) {
            val nodeIDList = nodeService.unarchiveNodes(nodeIDRequest)
            return ApiResponseHelper.generateStandardResponse(nodeIDList, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}

