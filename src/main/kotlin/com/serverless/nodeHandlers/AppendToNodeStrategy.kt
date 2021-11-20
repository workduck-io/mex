package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class AppendToNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error appending to node!"
        val json = input.body
        val pathParameters = input.pathParameters

        return if (pathParameters != null) {
            val nodeID = pathParameters.getOrDefault("id", "")

            val map: Map<String, Any>? = nodeService.append(nodeID, json)

            ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
