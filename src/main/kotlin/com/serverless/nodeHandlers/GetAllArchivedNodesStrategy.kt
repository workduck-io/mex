package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class GetAllArchivedNodesStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting archived node information"

        val workspaceID = input.pathParameters?.id

        return if(workspaceID != null) {
            val nodeIDList: MutableList<String>? = nodeService.getMetaDataOfAllArchivedNodesOfWorkspace(workspaceID)
            ApiResponseHelper.generateStandardResponse(nodeIDList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
