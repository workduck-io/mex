package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.utils.CommentHelper
import com.workduck.service.CommentService

class GetAllCommentsOfBlockStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error getting comments"

        val nodeID = input.pathParameters?.nodeID

        val blockID = input.pathParameters?.blockID

        return if (nodeID != null && blockID != null) {
            val commentList = commentService.getAllCommentsOfBlock(nodeID, blockID)

            val commentResponseList = mutableListOf<Response?>()
            commentList.map {
                commentResponseList.add(CommentHelper.convertCommentToCommentResponse(it))
            }

            ApiResponseHelper.generateStandardResponse(commentResponseList as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }

    }
}
