package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.UserStarService

interface UserStarStrategy {

    fun apply(input: Input, userStarService: UserStarService): ApiGatewayResponse
}
