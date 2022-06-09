package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.CommentHelper
import com.serverless.utils.Messages
import com.serverless.utils.withNotFoundException
import com.workduck.models.Comment
import com.workduck.service.CommentService

class GetCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val list = input.pathParameters?.id?.split("$")

        /* entity ID can be either BlockID or NodeID */
        val entityID = list?.get(0)
        val commentID = list?.get(1)

        return if(entityID != null  && commentID != null && CommentHelper.isBlockOrNodeID(entityID)) {
            val comment = commentService.getComment(entityID, commentID).withNotFoundException() as Comment

            val commentResponse: Response? = CommentHelper.convertCommentToCommentResponse(comment)

            ApiResponseHelper.generateStandardResponse(commentResponse, Messages.ERROR_GETTING_COMMENT)
        } else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.INVALID_ID, 400)
        }

    }

}