package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.UserService

class GetUserStrategy : UserStrategy {
    override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
        val errorMessage = "Error getting user"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        return if (pathParameters != null) {
            val userID = pathParameters["id"] as String

            val user: Entity? = userService.getUser(userID)
            ApiResponseHelper.generateStandardResponse(user as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
