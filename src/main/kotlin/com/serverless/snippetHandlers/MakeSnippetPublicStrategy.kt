package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.SnippetHelper.getSnippetVersionWithLatestAllowed
import com.workduck.service.SnippetService

class MakeSnippetPublicStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error making snippet public"

        /* will always be non-null since path has been matched */
        val version = input.pathParameters?.version?.let { getSnippetVersionWithLatestAllowed(it) }!!

        return input.pathParameters.id?.let { snippetID ->
            snippetService.makeSnippetPublic(snippetID, input.headers.workspaceID, version)
            ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
        } !!
    }

}
