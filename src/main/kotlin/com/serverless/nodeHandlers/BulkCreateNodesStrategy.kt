package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class BulkCreateNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node path"

        val request = input.payload

        return if(request != null){
            val x = nodeService.bulkCreateNodes(request, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(x, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }


}