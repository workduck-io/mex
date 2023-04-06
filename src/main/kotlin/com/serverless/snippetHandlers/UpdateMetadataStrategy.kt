package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.isValidSnippetID
import com.workduck.service.SnippetService

class UpdateMetadataStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val nodeID = input.pathParameters!!.id!!.let { id ->
            require(id.isValidSnippetID()) { Messages.INVALID_SNIPPET_ID }
            id
        }
        return input.payload?.let {
            snippetService.updateMetadataOfNode(it, nodeID, input.headers.workspaceID, input.tokenBody.userID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_SNIPPET_METADATA)
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST)
    }
}