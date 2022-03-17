package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.TokenBody
import com.serverless.utils.ExceptionParser
import com.workduck.service.NodeService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class NodeHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val nodeService = NodeService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        LOG.info(input)

        val isWarmup = Helper.isSourceWarmup(input["source"] as String?)

        if (isWarmup) {
            LOG.info("WarmUp - Lambda is warm!")
            return ApiResponseHelper.generateStandardResponse("Warming Up",  "")
        }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse("Error in Input", 500)

        val tokenBody: TokenBody = TokenBody.fromToken(wdInput.headers.bearerToken) ?: return ApiResponseHelper.generateStandardErrorResponse("Unauthorized", 401)

        if(!Helper.validateWorkspace(wdInput.headers.workspaceID, tokenBody.workspaceIDList)){
            return ApiResponseHelper.generateStandardErrorResponse("Not Authorized for the requested workspace",401)
        }

        val strategy = NodeStrategyFactory.getNodeStrategy(wdInput.routeKey)
                ?: return ApiResponseHelper.generateStandardErrorResponse("Request not recognized", 404)

        return try {
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