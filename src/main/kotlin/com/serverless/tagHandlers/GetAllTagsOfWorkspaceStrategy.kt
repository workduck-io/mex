package com.serverless.tagHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.TagService

class GetAllTagsOfWorkspaceStrategy : TagStrategy {
    override fun apply(input: Input, tagService: TagService): ApiGatewayResponse {
        val error = "Error fetching tags"
        return ApiResponseHelper.generateStandardResponse(tagService.getAllTagsOfWorkspace(input.headers.workspaceID), error)
    }

}
