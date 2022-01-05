package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.CommentService

class DeleteCommentStrategy : CommentStrategy{
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        val errorMessage = "Error deleting comment"

        val queryStringParameters = input.queryStringParameters

        val nodeID = queryStringParameters?.let{
            it["nodeID"].toString()
        }

        val blockID = queryStringParameters?.let {
            it["blockID"].toString()
        }

        val commentID = queryStringParameters?.let {
            it["commentID"].toString()
        }

        val returnedCommentID = commentService.deleteComment(nodeID, blockID, commentID)

        //val commentResponse: Response? = CommentHelper.convertCommentToCommentResponse(comment)

        //TODO(refactor this post error handling PR)
        return ApiResponseHelper.generateStandardResponse(returnedCommentID, errorMessage)
    }

}