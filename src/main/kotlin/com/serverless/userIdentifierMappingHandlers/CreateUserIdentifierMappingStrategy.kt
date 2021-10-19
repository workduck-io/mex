package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.UserIdentifierMappingService

class CreateUserIdentifierMappingStrategy : UserIdentifierMappingStrategy {
	override fun apply(
		input: Map<String, Any>,
		userIdentifierMappingService: UserIdentifierMappingService
	): ApiGatewayResponse {
		val errorMessage = "Error creating userIdentifierMapping"

		val json = input["body"] as String

		val userIdentifierMapping : Entity? = userIdentifierMappingService.createUserIdentifierRecord(json)

		return ApiResponseHelper.generateStandardResponse(userIdentifierMapping as Any?, errorMessage)
	}
}