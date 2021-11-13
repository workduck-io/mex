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
        val pathParameters = input["pathParameters"] as Map<*, *>?

        return if (pathParameters != null) {
            val userID = pathParameters["userID"] as String

            val userRecords: MutableList<String>? = userIdentifierMappingService.getUserRecords(userID)
            ApiResponseHelper.generateResponseWithJsonList(userRecords as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardResponse(null, errorMessage)
        }
    }
}
