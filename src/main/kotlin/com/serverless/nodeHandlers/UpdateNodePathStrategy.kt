package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class UpdateNodePathStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node path"

        val nodeToPathMapRequest = input.payload

        return if(nodeToPathMapRequest != null){
            val x = nodeService.updateNodePath(nodeToPathMapRequest)
            ApiResponseHelper.generateStandardResponse(x, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }


}