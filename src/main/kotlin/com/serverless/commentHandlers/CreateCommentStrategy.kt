package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.models.requests.WDRequest
import com.serverless.utils.CommentHelper
import com.serverless.utils.Messages
import com.workduck.service.CommentService

class CreateCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val commentRequest : WDRequest? = input.payload
        return if(commentRequest != null) {
            val commentResponse: Response? = commentService.createComment(commentRequest).let {
                CommentHelper.convertCommentToCommentResponse(it)
            }
            ApiResponseHelper.generateStandardResponse(commentResponse, 201, Messages.ERROR_CREATING_COMMENT)
        } else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)
        }
    }

}