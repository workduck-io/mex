package com.serverless.userHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserService

class GetUsersByNamespaceStrategy : UserStrategy {
    override fun apply(input: Map<String, Any>, userService: UserService): ApiGatewayResponse {
        val errorMessage = "Error getting users!"
        val pathParameters = input["pathParameters"] as Map<*, *>?
        return if (pathParameters != null) {
            val namespaceID = pathParameters["id"] as String

            val users: MutableList<String>? = userService.getAllUsersWithNamespaceID(namespaceID)

            ApiResponseHelper.generateStandardResponse(users as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
