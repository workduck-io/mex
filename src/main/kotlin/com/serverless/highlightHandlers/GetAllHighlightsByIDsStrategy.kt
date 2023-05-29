package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.HighlightService

class GetAllHighlightsByIDsStrategy: HighlightStrategy  {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        return input.payload?.let { highlightRequest ->
            highlightService.getAllHighlightsByIDs(highlightRequest, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_MULTIPLE_HIGHLIGHTS)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }

}