package com.serverless.commentHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.tagHandlers.TagHandler
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.Messages
import com.serverless.utils.handleWarmup
import com.workduck.service.CommentService
import org.apache.logging.log4j.LogManager

class CommentHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    var commentService = CommentService()
    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        val strategy = CommentStrategyFactory.getCommentStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.REQUEST_NOT_RECOGNIZED, 404)


        return try {
            validateTokenAndWorkspace(wdInput)
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