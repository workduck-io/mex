package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService

class CreateBookmarkInBatchStrategy : UserBookmarkStrategy {
    override fun apply(
            input: Input,
            userBookmarkService: UserBookmarkService
    ): ApiGatewayResponse {
        val errorMessage = "Error creating bookmarks"
        val nodeIDs = input.pathParameters?.ids
        val userID = input.pathParameters?.userID

        return if(userID != null && nodeIDs != null) {
            val nodeIDList: List<String> = nodeIDs.split(",")

            userBookmarkService.createBookmarksInBatch(userID, nodeIDList)
            ApiResponseHelper.generateStandardResponse(null, 201,  errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}