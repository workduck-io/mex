package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.extensions.getLastKeyFromQueryParam
import com.workduck.service.HighlightService

class GetAllHighlightsStrategy : HighlightStrategy {
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        val lastKey = input.getLastKeyFromQueryParam()
        return highlightService.getAllHighlights(input.headers.workspaceID, input.tokenBody.userID, lastKey).let {
            ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_ALL_HIGHLIGHTS)
        }
    }
}