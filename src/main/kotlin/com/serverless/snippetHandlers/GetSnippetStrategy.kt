package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class GetSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val version = input.queryStringParameters?.let{
            it["version"]?.toInt()
        }

        return input.pathParameters?.id?.let { snippetID ->
            snippetService.getSnippet(snippetID, input.headers.workspaceID, version).let {
                ApiResponseHelper.generateStandardResponse(SnippetHelper.convertSnippetToSnippetResponse(it), Messages.ERROR_GETTING_SNIPPET)
            }
        }!!

    }
}
