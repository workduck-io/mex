package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.models.WDRequest
import com.serverless.utils.CommentHelper
import com.workduck.service.CommentService

class CreateCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {

        val errorMessage = "Error creating comment"
        val commentRequest : WDRequest? = input.payload

        return if(commentRequest != null) {
            val commentResponse: Response? = commentService.createComment(commentRequest).let {
                CommentHelper.convertCommentToCommentResponse(it)
            }
            ApiResponseHelper.generateStandardResponse(commentResponse, 201, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid request body", 400)
        }
    }

}