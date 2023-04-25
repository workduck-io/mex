package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.serverless.utils.extensions.getWorkspaceIDFromPathParam
import com.workduck.service.NodeService

class AdminCreateNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        val workspaceID = input.getWorkspaceIDFromPathParam()

        return input.payload?.let { nodeRequest ->
            nodeService.adminCreateAndUpdateNode(nodeRequest, input.headers.workspaceID, workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(NodeHelper.convertNodeToNodeResponse(it), Messages.ERROR_CREATING_NODE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)

    }
}
