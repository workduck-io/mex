package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidID
import com.workduck.service.NodeService

class DeleteArchivedNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val namespaceID = input.queryStringParameters?.let{
            it["namespaceID"]?.let{ namespaceID ->
                require(namespaceID.isValidID(Constants.NAMESPACE_ID_PREFIX)) { Messages.INVALID_NAMESPACE_ID }
                namespaceID
            }
        } ?: throw IllegalArgumentException(Messages.INVALID_NAMESPACE_ID)

        return input.payload?.let {
            nodeService.deleteArchivedNodes(it, input.headers.workspaceID, namespaceID, input.tokenBody.userID).let { nodeIDList ->
                ApiResponseHelper.generateStandardResponse(nodeIDList,  Messages.ERROR_DELETING_NODE)
            }
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST)

    }
}
