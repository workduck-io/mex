package com.serverless.commentHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.workduck.service.CommentService
import org.apache.logging.log4j.LogManager

class CommentHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    var commentService = CommentService()
    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        val wdInput : Input = Input.fromMap(input)

        val strategy = CommentStrategyFactory.getCommentStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }

        return strategy.apply(wdInput, commentService)
    }

    companion object {
        private val LOG = LogManager.getLogger(CommentHandler::class.java)
    }

}