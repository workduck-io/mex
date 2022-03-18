package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.UserService

interface UserStrategy {
    fun apply(input: Input, userService: UserService): ApiGatewayResponse
}
