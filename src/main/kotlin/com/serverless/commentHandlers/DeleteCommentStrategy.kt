package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.CommentHelper
import com.serverless.utils.Messages
import com.workduck.service.CommentService

class DeleteCommentStrategy : CommentStrategy{
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val list = input.pathParameters?.id?.split("$")

        val entityID = list?.get(0)
        val commentID = list?.get(1)

        return if(entityID != null  && commentID != null && CommentHelper.isBlockOrNodeID(entityID)) {
            commentService.deleteComment(entityID, commentID)
            ApiResponseHelper.generateStandardResponse(null, 204, Messages.ERROR_DELETING_COMMENT)
        } else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.INVALID_ID, 400)
        }

    }

}