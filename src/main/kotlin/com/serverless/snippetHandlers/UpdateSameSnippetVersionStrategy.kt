package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.SnippetHelper
import com.workduck.service.SnippetService

class UpdateSameSnippetVersionStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error updating snippet"

        val version = input.pathParameters?.version?.let { SnippetHelper.getSnippetVersion(it) } !!
        return input.payload?.let { snippetRequest ->
            snippetService.updateSnippet(snippetRequest, version, input.tokenBody.userID, input.headers.workspaceID).let {
                ApiResponseHelper.generateStandardResponse(null,204, errorMessage)
            }
        } ?: throw IllegalArgumentException("Malformed Request")
    }

}
