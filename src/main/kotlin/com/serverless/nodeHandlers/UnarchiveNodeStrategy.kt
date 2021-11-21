package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class UnarchiveNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error un-archiving node"

        val bodyJson = input["body"] as String
        val returnedNodeIDList: MutableList<String> = nodeService.unarchiveNodes(bodyJson)
        return ApiResponseHelper.generateStandardResponse(returnedNodeIDList as Any?, errorMessage)
    }
}

