package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class CreateNewVersionStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error creating snippet"

        return input.payload?.let { snippetRequest ->
            snippetService.createNextSnippetVersion(snippetRequest, input.tokenBody.email, input.headers.workspaceID)?.let {
                ApiResponseHelper.generateStandardResponse(SnippetHelper.convertSnippetToSnippetResponse(it), errorMessage)
            }
        } ?: throw IllegalArgumentException("Malformed Request")
    }

}
