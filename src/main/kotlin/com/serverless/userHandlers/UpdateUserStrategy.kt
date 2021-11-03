package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.UserService

class UpdateUserStrategy : UserStrategy {
    override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
        val errorMessage = "Error updating user"
        val json = input["body"] as String

        val user: Entity? = userService.updateUser(json)
        return ApiResponseHelper.generateStandardResponse(user as Any?, errorMessage)
    }
}
