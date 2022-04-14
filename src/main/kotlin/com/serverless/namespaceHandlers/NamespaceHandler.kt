package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.workduck.service.NamespaceService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NamespaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val namespaceService = NamespaceService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val isWarmup = Helper.isSourceWarmup(input["source"] as String?)

        if (isWarmup) {
            LOG.info("WarmUp - Lambda is warm!")
            return ApiResponseHelper.generateStandardResponse("Warming Up",  "")
        }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)


        val strategy = NamespaceStrategyFactory.getNamespaceStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
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

    companion object {
        private val LOG = LogManager.getLogger(NamespaceHandler::class.java)
    }
}
