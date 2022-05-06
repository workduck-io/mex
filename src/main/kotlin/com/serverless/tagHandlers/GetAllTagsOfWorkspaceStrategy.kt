package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.TagService

class GetAllTagsOfWorkspaceStrategy : TagStrategy {
    override fun apply(input: Input, tagService: TagService): ApiGatewayResponse {
        return ApiResponseHelper.generateStandardResponse(tagService.getAllTagsOfWorkspace(input.headers.workspaceID), Messages.ERROR_GETTING_TAGS)
    }

}
