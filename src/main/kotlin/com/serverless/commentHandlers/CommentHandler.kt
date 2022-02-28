package com.serverless.commentHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.utils.ExceptionParser
import com.workduck.service.CommentService
import org.apache.logging.log4j.LogManager

class CommentHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    var commentService = CommentService()
    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Error in Input", 500)

        val strategy = CommentStrategyFactory.getCommentStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 404)


        return try {
            strategy.apply(wdInput, commentService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }

    }

    companion object {
        private val LOG = LogManager.getLogger(CommentHandler::class.java)
    }

}