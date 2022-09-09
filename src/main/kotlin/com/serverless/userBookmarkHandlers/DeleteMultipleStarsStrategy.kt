package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.UserStarService

class DeleteMultipleStarsStrategy : UserStarStrategy {
    override fun apply(
            input: Input,
            userStarService: UserStarService
    ): ApiGatewayResponse {
        return input.payload?.let { listRequest ->
            userStarService.deleteMultipleStars(input.tokenBody.userID, listRequest, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(null, 204,  Messages.ERROR_DELETING_STARRED)
        } ?: ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_DELETING_STARRED)
    }
}