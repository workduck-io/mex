package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.workduck.service.UserService

interface UserStrategy {
	fun apply(input: Map<String, Any>, userService : UserService) : ApiGatewayResponse
}