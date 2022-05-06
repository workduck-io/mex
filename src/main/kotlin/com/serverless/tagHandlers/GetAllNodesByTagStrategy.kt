package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.TagService

class GetAllNodesByTagStrategy : TagStrategy {
    override fun apply(input: Input, tagService: TagService): ApiGatewayResponse {
        return input.pathParameters?.tagName?.let {
            ApiResponseHelper.generateStandardResponse(tagService.getAllNodesByTag(it, input.headers.workspaceID), Messages.ERROR_GETTING_NODES)
        }!! /* tag name can't be null since path is being matched */

    }

}
