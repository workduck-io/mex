package com.serverless.snippetHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.SnippetService

class GetPublicSnippetStrategy : SnippetStrategy {
    override fun apply(input: Input, snippetService: SnippetService): ApiGatewayResponse {
        TODO("Not yet implemented")
    }

}
