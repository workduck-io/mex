package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Identifier
import com.workduck.service.NodeService

class DeleteArchivedNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error deleting unarchived node"

        val bodyJson = input["body"] as String
        val returnedNodeIDList: MutableList<String> = nodeService.deleteArchivedNodes(bodyJson)
        return ApiResponseHelper.generateStandardResponse(returnedNodeIDList as Any?, errorMessage)
    }
}
