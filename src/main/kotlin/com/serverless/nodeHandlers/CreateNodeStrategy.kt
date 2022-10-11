package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class CreateNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        return input.payload?.let { nodeRequest ->
            nodeService.createAndUpdateNode(nodeRequest, input.headers.workspaceID, input.tokenBody.userID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_CREATING_NODE)
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)

    }
}
