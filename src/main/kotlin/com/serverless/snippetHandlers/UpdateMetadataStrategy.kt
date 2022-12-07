package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Constants
import com.serverless.utils.Messages
import com.serverless.utils.isValidID
import com.workduck.service.SnippetService

class UpdateMetadataStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val nodeID = input.pathParameters!!.id!!.let { id ->
            require(id.isValidID(Constants.SNIPPET_ID_PREFIX)) { Messages.INVALID_SNIPPET_ID }
            id
        }
        return input.payload?.let {
            snippetService.updateMetadataOfNode(it, nodeID, input.headers.workspaceID, input.tokenBody.userID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_SNIPPET_METADATA)
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST)
    }
}