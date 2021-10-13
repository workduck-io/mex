package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserService

class GetUsersByWorkspaceStrategy : UserStrategy {
	override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
		val errorMessage = "Error getting users!"
		val pathParameters = input["pathParameters"] as Map<*, *>?
		val workspaceID = pathParameters!!["id"] as String

		val users: MutableList<String>? = userService.getAllUsersWithWorkspaceID(workspaceID)
		return ApiResponseHelper.generateStandardResponse(users as Any?, errorMessage)
	}
}