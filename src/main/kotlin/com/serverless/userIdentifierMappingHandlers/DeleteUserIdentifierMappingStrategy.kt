package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserIdentifierMappingService

class DeleteUserIdentifierMappingStrategy : UserIdentifierMappingStrategy {
    override fun apply(
        input: Map<String, Any>,
        userIdentifierMappingService: UserIdentifierMappingService
    ): ApiGatewayResponse {
        val errorMessage = "Error deleting userIdentifierMapping"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        return if (pathParameters != null) {
            val userID = pathParameters["userID"] as String
            val identifierID = pathParameters["identifierID"] as String

            val map: Map<String, String>? = userIdentifierMappingService.deleteUserIdentifierMapping(userID, identifierID)
            ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
