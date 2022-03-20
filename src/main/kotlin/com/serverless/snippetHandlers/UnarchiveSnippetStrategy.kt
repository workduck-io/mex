package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.SnippetService

class UnarchiveSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error un-archiving snippets"
        return input.payload?.let { snippetIDRequest ->
            ApiResponseHelper.generateStandardResponse(snippetService.unarchiveSnippets(snippetIDRequest), errorMessage)
        } ?: throw IllegalArgumentException("Invalid Body")
    }

}
