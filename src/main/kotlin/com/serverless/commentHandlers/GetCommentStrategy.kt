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

        val comment = commentService.getComment(nodeID, blockID, commentID)

        val commentResponse: Response? = CommentHelper.convertCommentToCommentResponse(comment)

        return ApiResponseHelper.generateStandardResponse(commentResponse, errorMessage)

    }

}