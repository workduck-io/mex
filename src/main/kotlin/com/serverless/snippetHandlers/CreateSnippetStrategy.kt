package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class CreateSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val createNextVersion = input.queryStringParameters?.let{
            it["createNextVersion"]?.toBoolean()
        } ?: false

        return input.payload?.let { snippetRequest ->
            snippetService.createAndUpdateSnippet(snippetRequest, input.tokenBody.userID, input.headers.workspaceID, createNextVersion).let {
                ApiResponseHelper.generateStandardResponse(SnippetHelper.convertSnippetToSnippetResponse(it), Messages.ERROR_CREATING_SNIPPET)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }

}
