package com.serverless.snippetHandlers

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.TokenBody
import com.serverless.tagHandlers.TagHandler
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.handleWarmup
import com.workduck.service.SnippetService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class SnippetHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val snippetService = SnippetService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)

        validateTokenAndWorkspace(wdInput)

        val strategy = SnippetStrategyFactory.getSnippetStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 404)

        return try {
            strategy.apply(wdInput, snippetService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(SnippetHandler::class.java)
    }
}