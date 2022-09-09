package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.Messages
import com.workduck.service.UserStarService

class CreateStarStrategy: UserStarStrategy {
    override fun apply(
            input: Input,
            userStarService: UserStarService
    ): ApiGatewayResponse {
        return input.pathParameters!!.id!!.let { nodeID ->
            userStarService.createStar(input.tokenBody.userID, nodeID, input.headers.workspaceID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_CREATING_STARRED)
        }

    }
}