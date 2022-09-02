package com.serverless.tagHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.module.kotlin.readValue
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.handleWarmup
import com.workduck.service.TagService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class TagHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    companion object {
        private val tagService = TagService()

        private val LOG = LogManager.getLogger(TagHandler::class.java)

        val json = """
            {
                "ids" : ["xyz"]
            }
        """.trimIndent()
        val payload: WDRequest? = Helper.objectMapper.readValue(json)

        private val sampleInput =  Input.fromMap(mutableMapOf("headers" to "[]"))

        private val dummyStrategy = TagStrategyFactory.getTagStrategy("")
    }


    override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

        LOG.info(wdInput.routeKey)
        LOG.info("Username: ${wdInput.tokenBody.username}")
        val strategy = TagStrategyFactory.getTagStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 400)


        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, tagService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }

    }
}