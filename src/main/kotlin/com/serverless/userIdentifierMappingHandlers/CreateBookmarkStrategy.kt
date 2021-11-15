package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserIdentifierMappingService

class CreateBookmarkStrategy: UserIdentifierMappingStrategy {
    override fun apply(
            input: Map<String, Any>,
            userIdentifierMappingService: UserIdentifierMappingService
    ): ApiGatewayResponse {
        val errorMessage = "Error creating bookmark"
        val pathParameters = input["pathParameters"] as Map<*, *>?
        val userID = pathParameters!!["userID"] as String
        val nodeID = pathParameters["nodeID"] as String

        val returnedNodeID: String? = userIdentifierMappingService.createBookmark(userID, nodeID)
        return ApiResponseHelper.generateResponseWithJsonList(returnedNodeID as Any?, errorMessage)
    }
}