package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.ExceptionParser
import com.serverless.utils.Helper.validateTokenAndWorkspace
import com.serverless.utils.handleWarmup
import com.serverless.utils.Messages
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NodeHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val nodeService = NodeService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        LOG.info(wdInput.routeKey)
        val strategy = NodeStrategyFactory.getNodeStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.REQUEST_NOT_RECOGNIZED, 404)

        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, nodeService)
        }
        catch(e : Exception){
            ExceptionParser.exceptionHandler(e)
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(NodeHandler::class.java)
    }
}