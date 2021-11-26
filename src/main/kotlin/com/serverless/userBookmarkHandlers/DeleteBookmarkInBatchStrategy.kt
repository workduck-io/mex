package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService

class DeleteBookmarkInBatchStrategy : UserBookmarkStrategy {
    override fun apply(
            input: Input,
            userBookmarkService: UserBookmarkService
    ): ApiGatewayResponse {
        val errorMessage = "Error deleting bookmarks"
        val nodeIDs = input.pathParameters?.ids
        val userID = input.pathParameters?.userID

        return if(userID != null && nodeIDs != null) {
            val nodeIDList: List<String> = nodeIDs.split(",")

            val returnedNodeIDList: List<String>? = userBookmarkService.deleteBookmarksInBatch(userID, nodeIDList)
            ApiResponseHelper.generateStandardResponse(returnedNodeIDList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}