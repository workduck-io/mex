package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.UserService

class CreateUserStrategy : UserStrategy {
	override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
		val errorMessage = "Error creating user"

		val json = input["body"] as String
		val user: Entity? = userService.createUser(json)
		return ApiResponseHelper.generateStandardResponse(user as Any?, errorMessage)
	}
}