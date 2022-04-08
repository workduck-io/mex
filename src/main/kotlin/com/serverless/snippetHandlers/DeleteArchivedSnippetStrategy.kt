package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.SnippetService

class DeleteArchivedSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error deleting snippets"
        return input.payload?.let { snippetIDRequest ->
            ApiResponseHelper.generateStandardResponse(snippetService.deleteArchivedSnippets(snippetIDRequest, input.headers.workspaceID), errorMessage)
        } ?: throw IllegalArgumentException("Malformed Request")
    }

}
