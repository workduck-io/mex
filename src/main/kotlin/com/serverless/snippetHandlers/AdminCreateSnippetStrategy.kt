package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.serverless.utils.SnippetHelper
import com.serverless.utils.extensions.getBooleanFromQueryParam
import com.serverless.utils.extensions.getWorkspaceIDFromPathParam
import com.workduck.service.SnippetService

class AdminCreateSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val createNextVersion = input.getBooleanFromQueryParam("createNextVersion")
        val workspaceID = input.getWorkspaceIDFromPathParam()

        return input.payload?.let { snippetRequest ->
            snippetService.adminCreateAndUpdateSnippet(snippetRequest, input.tokenBody.userID, input.headers.workspaceID, workspaceID, createNextVersion).let {
                ApiResponseHelper.generateStandardResponse(SnippetHelper.convertSnippetToSnippetResponse(it), Messages.ERROR_CREATING_SNIPPET)
            }
        } ?: throw IllegalArgumentException(Messages.MALFORMED_REQUEST)
    }

}
