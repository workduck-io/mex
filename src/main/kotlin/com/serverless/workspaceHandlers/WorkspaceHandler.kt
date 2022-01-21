package com.serverless.workspaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.serverless.models.Input
import com.workduck.service.WorkspaceService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class WorkspaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val workspaceService = WorkspaceService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val isWarmup = Helper.isSourceWarmup(input["source"] as String?)

        if (isWarmup) {
            LOG.info("WarmUp - Lambda is warm!")
            return ApiResponseHelper.generateStandardResponse("Warming Up",  "")
        }

        val wdInput : Input = Input.fromMap(input)

        val strategy = WorkspaceStrategyFactory.getWorkspaceStrategy(wdInput.routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }

        return strategy.apply(wdInput, workspaceService)
    }

    companion object {
        private val LOG = LogManager.getLogger(WorkspaceHandler::class.java)
    }
}
