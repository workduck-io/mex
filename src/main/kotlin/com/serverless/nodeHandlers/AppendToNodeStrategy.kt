package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class AppendToNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error appending to node!"

        val nodeID = input.pathParameters?.id

        val elementListRequest = input.payload

        //TODO(create an ElementResponse object. And , make NodeResponse.data of the type List<ElementResponse>)
        return if (nodeID != null && elementListRequest!= null) {

            val map: Map<String, Any>? = nodeService.append(nodeID, input.tokenBody.email,elementListRequest)

            ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
