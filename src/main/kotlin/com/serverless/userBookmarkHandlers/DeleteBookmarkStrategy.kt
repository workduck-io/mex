package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService

class DeleteBookmarkStrategy: UserBookmarkStrategy {
    override fun apply(
            input: Input,
            userBookmarkService: UserBookmarkService
    ): ApiGatewayResponse {
        val errorMessage = "Error deleting bookmark"

        val userID = input.pathParameters?.userID
        val nodeID = input.pathParameters?.nodeID

        return if (userID != null && nodeID != null) {
            val returnedNodeID: String? = userBookmarkService.deleteBookmark(userID, nodeID)
            ApiResponseHelper.generateResponseWithJsonList(returnedNodeID as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}