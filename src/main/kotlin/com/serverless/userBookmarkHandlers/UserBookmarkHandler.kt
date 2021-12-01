package com.serverless.userBookmarkHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.ExceptionParser
import com.workduck.service.UserBookmarkService
import org.apache.logging.log4j.LogManager

class UserBookmarkHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val userBookmarkService = UserBookmarkService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val wdInput: Input = Input.fromMap(input)

        val strategy = UserBookmarkStrategyFactory.getUserBookmarkStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 404)

        return try {
            strategy.apply(wdInput, userBookmarkService)
        } catch (e: Exception) {
            ExceptionParser.exceptionHandler(e)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(UserBookmarkHandler::class.java)
    }
}
