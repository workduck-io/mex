package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.AdvancedElement
import com.workduck.service.NodeService

class UpdateNodeBlockStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {

        val errorMessage = "Error updating node block"

        val json = input["body"] as String

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val nodeID = pathParameters!!["id"] as String

        val element: AdvancedElement? = nodeService.updateNodeBlock(nodeID, json)
        return ApiResponseHelper.generateStandardResponse(element as Any?, errorMessage)
    }
}
