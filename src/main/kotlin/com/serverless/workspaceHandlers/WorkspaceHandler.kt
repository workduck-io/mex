package com.serverless.workspaceHandlers

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
import com.serverless.utils.Messages
import com.serverless.utils.extensions.handleWarmup
import com.workduck.service.WorkspaceService
import com.workduck.utils.Helper
import org.apache.logging.log4j.LogManager

class WorkspaceHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    companion object {
        private val workspaceService = WorkspaceService()

        private val LOG = LogManager.getLogger(WorkspaceHandler::class.java)

        val json = """
            {
                "ids" : ["xyz"]
            }
        """.trimIndent()
        val payload: WDRequest? = Helper.objectMapper.readValue(json)

        private val sampleInput =  Input.fromMap(mutableMapOf("headers" to "[]"))

        private val dummyStrategy = WorkspaceStrategyFactory.getWorkspaceStrategy("")
    }

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        input.handleWarmup(LOG)?.let{ return it }

        val wdInput : Input = Input.fromMap(input) ?: return ApiResponseHelper.generateStandardErrorResponse(Messages.MALFORMED_REQUEST, 400)

        LOG.info(wdInput.myRouteKey)
        LOG.info("Username: ${wdInput.tokenBody.username}")
        val strategy = WorkspaceStrategyFactory.getWorkspaceStrategy(wdInput.myRouteKey)

        if (strategy == null) {
            val responseBody = StandardResponse(Messages.REQUEST_NOT_RECOGNIZED)
            return ApiGatewayResponse.build {
                statusCode = 400
                objectBody = responseBody
            }
        }

        return try {
            validateTokenAndWorkspace(wdInput)
            strategy.apply(wdInput, workspaceService)
        } catch (e: Exception) {
            ExceptionParser.exceptionHandler(e)
        }

    }
}
