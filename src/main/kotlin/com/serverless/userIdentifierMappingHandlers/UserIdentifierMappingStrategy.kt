package com.serverless.userIdentifierMappingHandlers

import com.serverless.ApiGatewayResponse
import com.workduck.service.UserIdentifierMappingService

interface UserIdentifierMappingStrategy {

    fun apply(input: Map<String, Any>, userIdentifierMappingService: UserIdentifierMappingService): ApiGatewayResponse
}
