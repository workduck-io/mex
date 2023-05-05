package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getHighlightLastKeyFromQueryParam
import com.workduck.service.HighlightService

class GetAllHighlightsStrategy : HighlightStrategy {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        return highlightService.getAllHighlights(input.headers.workspaceID, input.tokenBody.userID).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_ALL_HIGHLIGHTS)
        }
    }
}