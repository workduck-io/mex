package com.serverless.userPreferenceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.handleWarmup
import com.serverless.utils.Messages
import com.workduck.service.UserPreferenceService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class UserPreferenceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val userPreferenceService = UserPreferenceService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        LOG.info(wdInput.routeKey)
        val strategy = UserPreferenceStrategyFactory.getUserPreferenceStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse(Messages.REQUEST_NOT_RECOGNIZED)
            return ApiGatewayResponse.build {
                statusCode = 400
                objectBody = responseBody
            }
        }

        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, userPreferenceService)
        } catch (e: Exception) {
            ExceptionParser.exceptionHandler(e)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(UserPreferenceHandler::class.java)
    }
}
