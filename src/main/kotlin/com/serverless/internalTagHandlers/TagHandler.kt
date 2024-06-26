package com.serverless.internalTagHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.tagHandlers.TagHandler
import com.serverless.utils.ExceptionParser
import com.workduck.service.TagService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class TagHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    private val tagService = TagService()

    override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {
        LOG.info(input)
        val isWarmup = Helper.isSourceWarmup(input["source"] as String?)


        if (isWarmup) {
            LOG.info("WarmUp - Lambda is warm!")
            return ApiResponseHelper.generateStandardResponse("Warming Up",  "")
        }

        val wdInput : TagInput = TagInput.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

        LOG.info(wdInput.routeKey)
        val strategy = InternalTagStrategyFactory.getTagStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 404)


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
