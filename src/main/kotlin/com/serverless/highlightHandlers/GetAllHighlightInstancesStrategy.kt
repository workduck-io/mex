package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.HighlightService

class GetAllHighlightInstancesStrategy: HighlightStrategy{
    override fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse {
        return input.pathParameters!!.id!!.let { highlightID ->
            highlightService.getAllHighlightInstances(input.headers.workspaceID, input.tokenBody.userID, highlightID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_ALL_HIGHLIGHT_INSTANCES)
            }
        }
    }
}