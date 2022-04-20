package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.SnippetService

class DeleteAllVersionsOfSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error deleting snippets"


        return input.pathParameters?.id?.let { snippetID ->
            snippetService.deleteAllVersionsOfSnippet(snippetID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
            }
        }!! /* id will always exist ( non-null ) since path is being matched */

    }
}