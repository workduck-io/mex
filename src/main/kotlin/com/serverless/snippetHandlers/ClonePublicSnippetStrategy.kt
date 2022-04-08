package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class ClonePublicSnippetStrategy: SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error cloning snippet"

        return input.payload?.let { snippetRequest ->
            snippetService.clonePublicSnippet(snippetRequest, input.tokenBody.email, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(SnippetHelper.convertSnippetToSnippetResponse(it), errorMessage)
            }
        } ?: throw IllegalArgumentException("No ID passed")
    }
}