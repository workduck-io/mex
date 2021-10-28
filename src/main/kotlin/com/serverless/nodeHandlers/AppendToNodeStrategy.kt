package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class AppendToNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error appending to node!"
        val json = input["body"] as String
        val pathParameters = input["pathParameters"] as Map<*, *>?
        val nodeID = pathParameters!!["id"] as String

        val map: Map<String, Any>? = nodeService.append(nodeID, json)

        return ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)
    }
}
