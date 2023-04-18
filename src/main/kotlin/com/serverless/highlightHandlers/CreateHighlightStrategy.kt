package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.HighlightService

class CreateHighlightStrategy: HighlightStrategy  {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        return input.payload?.let { highlightRequest ->
            highlightService.createHighlight(highlightRequest, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_CREATING_HIGHLIGHT)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }

}