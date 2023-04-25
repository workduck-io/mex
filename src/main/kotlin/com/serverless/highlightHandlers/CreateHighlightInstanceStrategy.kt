package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getHighlightIDFromPathParam
import com.workduck.service.HighlightService

class CreateHighlightInstanceStrategy: HighlightStrategy {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        val highlightID = input.getHighlightIDFromPathParam()
        return input.payload?.let { highlightInstanceRequest ->
            highlightService.createHighlightInstance(highlightInstanceRequest, input.tokenBody.userID, input.headers.workspaceID, highlightID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_CREATING_HIGHLIGHT_INSTANCE)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }
}