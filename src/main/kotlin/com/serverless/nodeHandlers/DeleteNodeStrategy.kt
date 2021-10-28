package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Identifier
import com.workduck.service.NodeService

class DeleteNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error deleting node"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val nodeID = pathParameters!!["id"] as String

        val identifier: Identifier? = nodeService.deleteNode(nodeID)
        return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
    }
}
