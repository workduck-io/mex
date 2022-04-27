package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class AppendToNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val nodeID = input.pathParameters?.id

        val elementListRequest = input.payload

        //TODO(create an ElementResponse object. And , make NodeResponse.data of the type List<ElementResponse>)
        return if (nodeID != null && elementListRequest!= null) {

            val map: Map<String, Any>? = nodeService.append(nodeID, input.headers.workspaceID, input.tokenBody.userID,elementListRequest)

            ApiResponseHelper.generateStandardResponse(map as Any?, Messages.ERROR_APPENDING_NODE)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_APPENDING_NODE)
        }
    }
}
