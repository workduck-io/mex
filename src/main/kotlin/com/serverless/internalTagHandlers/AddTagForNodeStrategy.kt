package com.serverless.internalTagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.TagService

class AddTagForNodeStrategy : InternalTagStrategy {
    override fun apply(input: TagInput, tagService: TagService): ApiGatewayResponse {
        return input.payload.let {
            tagService.addNodeForTags(it, input.workspaceID)
            ApiResponseHelper.generateStandardResponse(null, 204, "")
        }
    }

}
