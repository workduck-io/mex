package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class GetAllSnippetsOfWorkspaceStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {

        val getData = input.queryStringParameters?.let{
            it["getData"].toBoolean()
        } ?: false

        return when(getData){
            false -> snippetService.getAllSnippetsMetadataOfWorkspace(input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_SNIPPETS)
            }
            true -> snippetService.getAllSnippetsDataOfWorkspace(input.headers.workspaceID).map { snippet ->
                SnippetHelper.convertSnippetToSnippetResponse(snippet) }.let {
                ApiResponseHelper.generateStandardResponse(it, Messages.ERROR_GETTING_SNIPPETS)
            }
        }

    }

}