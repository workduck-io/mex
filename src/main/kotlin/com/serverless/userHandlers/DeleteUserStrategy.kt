package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Identifier
import com.workduck.service.UserService

class DeleteUserStrategy : UserStrategy {
	override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
		val errorMessage = "Error deleting user"

		val pathParameters = input["pathParameters"] as Map<*, *>?
		val userID = pathParameters!!["id"] as String

		val identifier: Identifier? = userService.deleteUser(userID)
		return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
	}
}