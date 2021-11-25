package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService

class CreateBookmarkStrategy: UserBookmarkStrategy {
    override fun apply(
            input: Input,
            userBookmarkService: UserBookmarkService
    ): ApiGatewayResponse {
        val errorMessage = "Error creating bookmark"

        val userID = input.pathParameters?.userID
        val nodeID = input.pathParameters?.nodeID

        return if (userID != null && nodeID != null) {
            val returnedNodeID: String? = userBookmarkService.createBookmark(userID, nodeID)
            ApiResponseHelper.generateResponseWithJsonList(returnedNodeID as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}