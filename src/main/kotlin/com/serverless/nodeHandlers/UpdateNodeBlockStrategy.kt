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

        val elementListRequest = input.payload

        val nodeID = input.pathParameters?.id
        return if (nodeID != null && elementListRequest != null) {
            val element: AdvancedElement? = nodeService.updateNodeBlock(nodeID, input.tokenBody.userID, input.tokenBody.userID, elementListRequest)
            ApiResponseHelper.generateStandardResponse(element as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
