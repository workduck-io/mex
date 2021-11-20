package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.UserIdentifierMappingService

class CreateBookmarkInBatchStrategy : UserIdentifierMappingStrategy {
    override fun apply(
            input: Map<String, Any>,
            userIdentifierMappingService: UserIdentifierMappingService
    ): ApiGatewayResponse {
        val errorMessage = "Error creating bookmarks"
        val pathParameters = input["pathParameters"] as Map<*, *>?

        val userID = pathParameters!!["userID"] as String
        val nodeIDList: List<String> = (pathParameters["ids"] as String).split(",")

        val returnedNodeIDList: List<String>? = userIdentifierMappingService.createBookmarksInBatch(userID, nodeIDList)
        return ApiResponseHelper.generateResponseWithJsonList(returnedNodeIDList as Any?, errorMessage)
    }
}