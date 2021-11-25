package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService

interface UserBookmarkStrategy {

    fun apply(input: Input, userBookmarkService: UserBookmarkService): ApiGatewayResponse
}
