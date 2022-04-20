package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.serverless.models.TokenBody
import com.workduck.service.SnippetService

interface SnippetStrategy {
    fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse
}