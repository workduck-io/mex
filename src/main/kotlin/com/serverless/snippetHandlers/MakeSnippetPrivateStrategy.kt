package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.SnippetService

class MakeSnippetPrivateStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        val errorMessage = "Error making snippet private"

        return input.pathParameters?.id?.let { snippetID ->
            snippetService.makeSnippetPublic(snippetID)
            ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
        }!!

    }

}
