package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Messages
import com.workduck.service.CommentService

class UpdateCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val commentRequest : WDRequest? = input.payload

        return if(commentRequest != null) {
            commentService.updateComment(commentRequest)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_UPDATING_COMMENT)

        } else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)
        }

    }

}