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

        val pathParameters = input["pathParameters"] as Map<String, String>?
        return if (pathParameters != null) {
            val userID = pathParameters.getOrDefault("userID", "")
            val identifierID = pathParameters.getOrDefault("identifierID", "")

            val map: Map<String, String>? = userIdentifierMappingService.deleteUserIdentifierMapping(userID, identifierID)
            ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
