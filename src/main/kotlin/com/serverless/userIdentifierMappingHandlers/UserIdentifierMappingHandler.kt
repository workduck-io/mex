package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager

class UserIdentifierMappingHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    private val userIdentifierMappingService = UserIdentifierMappingService()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val routeKey = input["routeKey"] as String

        val strategy = UserIdentifierMappingStrategyFactory.getUserIdentifierMappingStrategy(routeKey)

        if (strategy == null) {
            val responseBody = StandardResponse("Request type not recognized")
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
            }
        }
        return strategy.apply(input, userIdentifierMappingService)
    }

    companion object {
        private val LOG = LogManager.getLogger(UserIdentifierMappingHandler::class.java)
    }
}
