package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.utils.CommentHelper
import com.workduck.service.CommentService

class GetCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {

        val errorMessage = "Error fetching comment"

        val list = input.pathParameters?.id?.split("-")

        val nodeID = list?.get(0)
        val blockID = list?.get(1)
        val commentID = list?.get(2)

        return if(nodeID != null && blockID != null && commentID != null) {
            val comment = commentService.getComment(nodeID, blockID, commentID)

            val commentResponse: Response? = CommentHelper.convertCommentToCommentResponse(comment)

            ApiResponseHelper.generateStandardResponse(commentResponse, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid ID", 400)
        }

    }

}