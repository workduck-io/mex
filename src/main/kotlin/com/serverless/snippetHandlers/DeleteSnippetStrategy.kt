package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class DeleteSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val version = input.pathParameters?.version?.let { SnippetHelper.getSnippetVersion(it) } !!

        return input.pathParameters.id?.let { snippetID ->
            snippetService.deleteSnippetVersion(snippetID, version, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_SNIPPET)
            }
        }!! /* id will always exist ( non-null ) since path is being matched */

    }

}
