package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.AdvancedElement
import com.workduck.service.NodeService

class UpdateNodeBlockStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        val errorMessage = "Error updating node block"

        val json = input["body"] as String

        val pathParameters = input["pathParameters"] as Map<String, String>?
        return if (pathParameters != null) {
            val nodeID = pathParameters.getOrDefault("id", "")

            val element: AdvancedElement? = nodeService.updateNodeBlock(nodeID, json)
            ApiResponseHelper.generateStandardResponse(element as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
