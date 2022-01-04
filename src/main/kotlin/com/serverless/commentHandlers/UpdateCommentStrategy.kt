package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.models.WDRequest
import com.serverless.utils.CommentHelper
import com.workduck.models.Comment
import com.workduck.service.CommentService

class UpdateCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error updating comment"

        val commentRequest : WDRequest? = input.payload

        val comment : Comment? = commentService.updateComment(commentRequest)

        val commentResponse: Response? = CommentHelper.convertCommentToCommentResponse(comment)

        return ApiResponseHelper.generateStandardResponse(commentResponse, errorMessage)
    }

}