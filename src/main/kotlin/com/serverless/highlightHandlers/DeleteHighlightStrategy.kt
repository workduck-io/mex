package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getNamespaceIDFromQueryParam
import com.serverless.utils.extensions.getNodeIDFromQueryParam
import com.workduck.service.HighlightService

class DeleteHighlightStrategy: HighlightStrategy {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        val nodeID = input.getNodeIDFromQueryParam() ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
        val namespaceID = input.getNamespaceIDFromQueryParam() ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)

        return input.pathParameters?.id?.let { highlightID ->
            highlightService.deleteHighlight(highlightID, nodeID, namespaceID, input.headers.workspaceID, input.tokenBody.userID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_HIGHLIGHT)
            }
        }!!
    }
}