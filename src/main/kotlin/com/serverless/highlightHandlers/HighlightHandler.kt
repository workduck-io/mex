package com.serverless.highlightHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Messages
import com.serverless.utils.extensions.handleWarmup
import com.workduck.service.HighlightService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class HighlightHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    companion object {
        private val highlightService = HighlightService()
        private val LOG = LogManager.getLogger(HighlightHandler::class.java)

        val json = """
            {
                "ids" : ["xyz"]
            }
        """.trimIndent()
        val payload: WDRequest? = Helper.objectMapper.readValue(json)

        private val sampleInput = Input.fromMap(mutableMapOf("headers" to "[]"))

        private val dummyStrategy = HighlightStrategyFactory.getHighlightStrategy("")
    }

    override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {
        input.handleWarmup(LOG)?.let { return it }

        val wdInput: Input =
            Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        LOG.info(wdInput.myRouteKey)
        LOG.info("Username: ${wdInput.tokenBody.username}")
        val strategy = HighlightStrategyFactory.getHighlightStrategy(wdInput.myRouteKey)
            ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.REQUEST_NOT_RECOGNIZED, 400)


        return try {
            com.serverless.utils.Helper.validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, highlightService)
        } catch (e: Exception) {
            ExceptionParser.exceptionHandler(e)
        }
    }
}