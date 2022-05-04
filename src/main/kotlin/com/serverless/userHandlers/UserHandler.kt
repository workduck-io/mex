package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.tagHandlers.TagHandler
import com.serverless.utils.handleWarmup
import com.workduck.service.UserService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class UserHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val userService = UserService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

        val routeKey = wdInput.routeKey

        val strategy = UserStrategyFactory.getUserStrategy(routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }
        return strategy.apply(wdInput, userService)
    }

    companion object {
        private val LOG = LogManager.getLogger(UserHandler::class.java)
    }
}
