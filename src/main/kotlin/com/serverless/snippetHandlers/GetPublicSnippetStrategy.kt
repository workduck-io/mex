package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class GetPublicSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val version = input.pathParameters?.version?.let { SnippetHelper.getValidVersion(it) } !!

        return input.pathParameters.id?.let { snippetID ->
            snippetService.getPublicSnippet(snippetID, version)?.let {
                ApiResponseHelper.generateStandardResponse(SnippetHelper.convertSnippetToSnippetResponse(it), Messages.ERROR_GETTING_SNIPPET)
            } ?: throw IllegalArgumentException(Messages.INVALID_SNIPPET_ID)
        }!!
    }

}
