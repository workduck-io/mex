package com.serverless.userBookmarkHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService

class GetBookmarksStrategy : UserBookmarkStrategy {
    override fun apply(
            input: Input,
            userBookmarkService: UserBookmarkService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting bookmarks of the user"

        val userID = input.pathParameters?.userID
        return if(userID!= null) {
            val nodeIDList: MutableList<String>? = userBookmarkService.getAllBookmarkedNodesByUser(userID)
            ApiResponseHelper.generateStandardResponse(nodeIDList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }

    }
}