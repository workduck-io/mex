package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.CommentHelper
import com.workduck.service.CommentService

class GetCommentStrategy : CommentStrategy {
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {

        val errorMessage = "Error fetching comment"

        val list = input.pathParameters?.id?.split("$")

        /* entity ID can be either BlockID or NodeID */
        val entityID = list?.get(0)
        val commentID = list?.get(1)

        return if(entityID != null  && commentID != null && CommentHelper.isBlockOrNodeID(entityID)) {
            val comment = commentService.getComment(entityID, commentID)

            val commentResponse: Response? = CommentHelper.convertCommentToCommentResponse(comment)

            ApiResponseHelper.generateStandardResponse(commentResponse, errorMessage)
        } else{
            ApiResponseHelper.generateStandardErrorResponse("Invalid ID", 400)
        }

    }

}