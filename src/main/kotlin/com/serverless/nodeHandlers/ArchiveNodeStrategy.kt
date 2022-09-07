package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.NodeService

class ArchiveNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {

        /* since the path has been matched already, id cannot be null */
        return  input.pathParameters!!.id!!.let { namespaceID ->
            input.payload?.let { nodeIDsRequest ->
                val nodeIDList =nodeService.archiveNodes(nodeIDsRequest, input.headers.workspaceID, namespaceID)

                ApiResponseHelper.generateStandardResponse(nodeIDList, Messages.ERROR_ARCHIVING_NODE)
            } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_ARCHIVING_NODE)
        }

    }
}
