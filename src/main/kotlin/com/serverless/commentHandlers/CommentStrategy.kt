package com.serverless.commentHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.CommentService

interface CommentStrategy {
    fun apply(input: Input, commentService: CommentService): ApiGatewayResponse
}