package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class GetAllArchivedNodesStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting archived node information"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val workspaceID = pathParameters!!["id"] as String

        val nodeIDList: MutableList<String>? = nodeService.getMetaDataOfAllArchivedNodesOfWorkspace(workspaceID)
        return ApiResponseHelper.generateStandardResponse(nodeIDList as Any?, errorMessage)
    }
}
