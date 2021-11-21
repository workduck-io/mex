package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Node
import com.workduck.service.NodeService

class GetPublicNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Node not available"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val nodeID = pathParameters!!["id"] as String

        val node: Node? = nodeService.getPublicNode(nodeID)
        return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)
    }
}
