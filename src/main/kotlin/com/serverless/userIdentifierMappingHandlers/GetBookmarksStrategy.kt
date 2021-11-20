package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserIdentifierMappingService

class GetBookmarksStrategy : UserIdentifierMappingStrategy {
    override fun apply(
            input: Map<String, Any>,
            userIdentifierMappingService: UserIdentifierMappingService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting user records"
        val pathParameters = input["pathParameters"] as Map<*, *>?
        val userID = pathParameters!!["userID"] as String

        val nodeIDList: MutableList<String>? = userIdentifierMappingService.getAllBookmarkedNodesByUser(userID)
        return ApiResponseHelper.generateResponseWithJsonList(nodeIDList as Any?, errorMessage)
    }
}