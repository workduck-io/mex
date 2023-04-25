package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getHighlightIDFromPathParam
import com.workduck.service.HighlightService

class UpdateHighlightStrategy: HighlightStrategy {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        val id = input.getHighlightIDFromPathParam()
        return input.payload?.let { highlightRequest ->
            highlightService.updateHighlight(id, highlightRequest, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_HIGHLIGHT)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }
}