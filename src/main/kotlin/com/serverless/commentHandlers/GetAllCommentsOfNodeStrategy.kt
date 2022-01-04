package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.CommentService

class GetAllCommentsOfNodeStrategy : CommentStrategy{
    override fun apply(input: Input, commentService: CommentService): ApiGatewayResponse {
        TODO("Not yet implemented")
    }

}