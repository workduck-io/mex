package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class DeleteArchivedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error deleting unarchived node"

        val nodeIDRequest = input.payload

        return if(nodeIDRequest != null) {
            val returnedNodeIDList: MutableList<String> = nodeService.deleteArchivedNodes(nodeIDRequest)
            ApiResponseHelper.generateStandardResponse(returnedNodeIDList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
