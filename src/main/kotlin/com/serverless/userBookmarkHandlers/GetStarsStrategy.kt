package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.UserStarService

class GetStarsStrategy : UserStarStrategy {
    override fun apply(
            input: Input,
            userStarService: UserStarService
    ): ApiGatewayResponse {
        val nodeIDList: List<String> = userStarService.getAllBookmarkedNodesByUser(input.tokenBody.userID, input.headers.workspaceID)
        return ApiResponseHelper.generateStandardResponse(nodeIDList as Any, Messages.ERROR_GETTING_STARRED)
    }
}