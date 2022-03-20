package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.SnippetService

class GetAllArchivedSnippetsStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error getting archived snippets"

        return ApiResponseHelper.generateStandardResponse(snippetService.getMetaDataOfAllArchivedSnippetsOfWorkspace(input.headers.workspaceID), errorMessage)
    }
}
