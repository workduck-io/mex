package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class DeleteArchivedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val nodeIDRequest = input.payload

        return if(nodeIDRequest != null) {
            val returnedNodeIDList: MutableList<String> = nodeService.deleteArchivedNodes(nodeIDRequest, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(returnedNodeIDList as Any?, Messages.ERROR_DELETING_NODE)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_DELETING_NODE)
        }
    }
}
