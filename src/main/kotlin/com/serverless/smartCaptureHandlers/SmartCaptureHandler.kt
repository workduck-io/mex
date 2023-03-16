package com.serverless.smartCaptureHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.ExceptionParser
import com.serverless.utils.handleWarmup
import com.workduck.service.SmartCaptureService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager
import com.serverless.utils.Helper.validateTokenAndWorkspace

class SmartCaptureHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    companion object {
        private val smartCaptureService = SmartCaptureService()
        private val LOG = LogManager.getLogger(SmartCaptureHandler::class.java)

        val json = """
            {
                "ids" : ["xyz"]
            }
        """.trimIndent()
        val payload: WDRequest? = Helper.objectMapper.readValue(json)

        private val sampleInput =  Input.fromMap(mutableMapOf("headers" to "[]"))

        private val dummyStrategy = SmartCaptureStrategyFactory.getSmartCaptureStrategy("")
    }

    override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {
        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

        LOG.info(wdInput.routeKey)
        LOG.info("Username: ${wdInput.tokenBody.username}")
        val strategy = SmartCaptureStrategyFactory.getSmartCaptureStrategy(wdInput.routeKey)
            ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 400)


        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, smartCaptureService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }
    }
}