package com.serverless.tagHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.ExceptionParser
import com.serverless.utils.handleWarmup
import com.workduck.service.TagService
import org.apache.logging.log4j.LogManager

class TagHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    private val tagService = TagService()

    override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

        LOG.info(wdInput.routeKey)
        val strategy = TagStrategyFactory.getTagStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 400)


        return try {
            strategy.apply(wdInput, tagService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }

    }




    companion object {
        private val LOG = LogManager.getLogger(TagHandler::class.java)
    }
}