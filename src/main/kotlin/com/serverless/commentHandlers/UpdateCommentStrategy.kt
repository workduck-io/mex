package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.WDRequest
import com.workduck.service.CommentService

class UpdateCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error updating comment"

        val commentRequest : WDRequest? = input.payload

        return if(commentRequest != null) {
            commentService.updateComment(commentRequest)
            ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)

        } else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid request body", 400)
        }

    }

}