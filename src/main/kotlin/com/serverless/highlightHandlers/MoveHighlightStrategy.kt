package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.HighlightService

/* this will be used only when moving from highlight from default node to a given node */
class MoveHighlightStrategy : HighlightStrategy {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        return input.payload?.let { highlightRequest ->
            highlightService.moveHighlight(highlightRequest, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_MOVING_SMART_CAPTURE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }
}