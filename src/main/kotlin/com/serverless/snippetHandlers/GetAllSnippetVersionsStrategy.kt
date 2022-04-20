package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class GetAllSnippetVersionsStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error getting snippets"

        return input.pathParameters?.id?.let { snippetID ->
            snippetService.getAllVersionsOfSnippet(snippetID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(it, errorMessage)
            }
        }!!
    }

}