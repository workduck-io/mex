package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.TagService

class GetAllNodesByTagStrategy : TagStrategy {
    override fun apply(input: Input, tagService: TagService): ApiGatewayResponse {
        val error = "Error fetching nodes"

        return input.pathParameters?.tagName?.let {
            ApiResponseHelper.generateStandardResponse(tagService.getAllNodesByTag(it, input.headers.workspaceID), error)
        }!! /* tag name can't be null since path is being matched */

    }

}
