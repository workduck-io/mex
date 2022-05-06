package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.SnippetService

class DeleteAllVersionsOfSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        return input.pathParameters?.id?.let { snippetID ->
            snippetService.deleteAllVersionsOfSnippet(snippetID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_SNIPPETS)
            }
        }!! /* id will always exist ( non-null ) since path is being matched */

    }
}