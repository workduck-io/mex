package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.utils.CommentHelper
import com.workduck.service.CommentService

class GetAllCommentsStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error getting comments"

        /* when getting comments of a node , id = NodeID;
           when getting comments of a block, id = NodeID#BlockID */
        val compositeIDList = input.pathParameters?.id?.split("-")

        return if (compositeIDList != null && compositeIDList.size <= 2) {
            val commentList = commentService.getAllComments(compositeIDList)

            val commentResponseList = mutableListOf<Response?>()
            commentList.map {
                commentResponseList.add(CommentHelper.convertCommentToCommentResponse(it))
            }

            ApiResponseHelper.generateStandardResponse(commentResponseList as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse("Invalid ID", 400)
        }

    }
}
