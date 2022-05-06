package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.CommentHelper
import com.serverless.utils.Messages
import com.workduck.service.CommentService

class GetAllCommentsStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error getting comments"

        /* when getting comments of a node , id = NodeID
           when getting comments of a block, id = BlockID
           when getting comments of a user , id = UserID*/
        val id = input.pathParameters?.id

        return if (id != null && CommentHelper.isValidEntity(id)) {
            val commentList = commentService.getAllComments(id)

            val commentResponseList = mutableListOf<Response?>()
            commentList.map {
                commentResponseList.add(CommentHelper.convertCommentToCommentResponse(it))
            }

            ApiResponseHelper.generateStandardResponse(commentResponseList as Any?, Messages.ERROR_GETTING_COMMENTS)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(Messages.INVALID_ID, 400)
        }

    }
}
