package com.serverless.snippetHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.Messages
import com.serverless.utils.handleWarmup
import com.workduck.service.SnippetService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class SnippetHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    companion object {

        private val snippetService = SnippetService()

        private val LOG = LogManager.getLogger(SnippetHandler::class.java)

        val json = """
            {
                "ids" : []
            }
        """.trimIndent()
        val payload: WDRequest? = Helper.objectMapper.readValue(json)

        private val sampleInput =  Input.fromMap(mutableMapOf("headers" to "[]"))

        private val dummyStrategy = SnippetStrategyFactory.getSnippetStrategy("")
    }



    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        LOG.info(wdInput.routeKey)
        val strategy = SnippetStrategyFactory.getSnippetStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.REQUEST_NOT_RECOGNIZED, 400)

        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, snippetService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }
    }
}