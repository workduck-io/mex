package com.serverless.highlightHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.HighlightService

interface HighlightStrategy {
    fun apply(input: Input, highlightService: HighlightService): ApiGatewayResponse
}