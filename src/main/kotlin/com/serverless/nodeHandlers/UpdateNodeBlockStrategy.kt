package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.models.AdvancedElement
import com.workduck.service.NodeService

class UpdateNodeBlockStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        val errorMessage = "Error updating node block"

        val nodeBlock = input.body

        val nodeID = input.pathParameters?.id
        return if (nodeID != null && nodeBlock != null) {
            val element: AdvancedElement? = nodeService.updateNodeBlock(nodeID, nodeBlock)
            ApiResponseHelper.generateStandardResponse(element as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
