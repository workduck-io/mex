package com.serverless.userBookmarkHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.workduck.service.UserBookmarkService
import org.apache.logging.log4j.LogManager

class UserBookmarkHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val userBookmarkService = UserBookmarkService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val wdInput : Input = Input.fromMap(input)

        val strategy = UserBookmarkStrategyFactory.getUserBookmarkStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }
        return strategy.apply(wdInput, userBookmarkService)
    }

    companion object {
        private val LOG = LogManager.getLogger(UserBookmarkHandler::class.java)
    }
}
