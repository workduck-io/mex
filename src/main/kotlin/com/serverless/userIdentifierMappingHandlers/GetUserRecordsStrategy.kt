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
        val userID = pathParameters!!["userID"] as String

        val userRecords: MutableList<String>? = userIdentifierMappingService.getUserRecords(userID)
        return ApiResponseHelper.generateResponseWithJsonList(userRecords as Any?, errorMessage)
    }
}
