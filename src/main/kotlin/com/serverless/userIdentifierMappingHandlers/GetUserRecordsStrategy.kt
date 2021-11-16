package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserIdentifierMappingService

class GetUserRecordsStrategy : UserIdentifierMappingStrategy {
    override fun apply(
        input: Map<String, Any>,
        userIdentifierMappingService: UserIdentifierMappingService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting user records"
        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val userID = pathParameters.getOrDefault("userID", "")

            val userRecords: MutableList<String>? = userIdentifierMappingService.getUserRecords(userID)
            ApiResponseHelper.generateResponseWithJsonList(userRecords as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
