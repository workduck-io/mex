package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.CommentService

class DeleteCommentStrategy : CommentStrategy{
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error deleting comment"

        val list = input.pathParameters?.id?.split("-")

        val nodeID = list?.get(0)
        val blockID = list?.get(1)
        val commentID = list?.get(2)

        return if(nodeID != null && blockID != null && commentID != null) {
            commentService.deleteComment(nodeID, blockID, commentID)
            ApiResponseHelper.generateStandardResponse(null, 204, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid ID", 400)
        }

    }

}