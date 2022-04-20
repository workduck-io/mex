package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.TagService

class AddTagForNodeStrategy : TagStrategy {
    override fun apply(input: Input, tagService: TagService): ApiGatewayResponse {
        return input.internalPayLoad?.let {
            tagService.addNodeForTags(it, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(null, 204, "")
        } ?: throw IllegalArgumentException("Malformed Request")
    }

}
