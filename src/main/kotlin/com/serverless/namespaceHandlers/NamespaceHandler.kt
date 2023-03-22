package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.handleWarmup
import com.serverless.utils.Messages
import com.workduck.service.NamespaceService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NamespaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    companion object {
        private val namespaceService = NamespaceService()

        private val LOG = LogManager.getLogger(NamespaceHandler::class.java)

        val json = """
            {
                "ids" : ["xyz"]
            }
        """.trimIndent()
        val payload: WDRequest? = Helper.objectMapper.readValue(json)

        private val sampleInput =  Input.fromMap(mutableMapOf("headers" to "[]"))

        private val dummyStrategy = NamespaceStrategyFactory.getNamespaceStrategy("")
    }

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        LOG.info(wdInput.myRouteKey)
        LOG.info("Username: ${wdInput.tokenBody.username}")
        val strategy = NamespaceStrategyFactory.getNamespaceStrategy(wdInput.myRouteKey)

        if (strategy == null) {
            val responseBody = StandardResponse(Messages.REQUEST_NOT_RECOGNIZED)
            return ApiGatewayResponse.build {
                statusCode = 400
                objectBody = responseBody
            }
        }

        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, namespaceService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }

    }
}
