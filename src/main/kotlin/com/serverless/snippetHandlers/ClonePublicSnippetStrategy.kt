package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class ClonePublicSnippetStrategy: SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val version = input.pathParameters?.version?.let { SnippetHelper.getValidVersion(it) } !!

        return input.pathParameters.id?.let { snippetID ->
            snippetService.clonePublicSnippet(snippetID, version, input.tokenBody.userID, input.headers.workspaceID).let { newSnippetID ->
                ApiResponseHelper.generateStandardResponse(newSnippetID, Messages.ERROR_CLONING_SNIPPET)
            }
        }!! /* id will always exist ( non-null ) since path is being matched */

    }
}