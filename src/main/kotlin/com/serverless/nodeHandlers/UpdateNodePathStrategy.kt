package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class UpdateNodePathStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node path"

        val nodePathRefactorRequest = input.payload

        return if(nodePathRefactorRequest != null){
            val x = nodeService.updateNodePath(nodePathRefactorRequest)
            ApiResponseHelper.generateStandardResponse(x, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }


}