package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.workduck.service.UserPreferenceService

interface UserPreferenceStrategy {

    fun apply(input: Map<String, Any>, userPreferenceService: UserPreferenceService): ApiGatewayResponse
}