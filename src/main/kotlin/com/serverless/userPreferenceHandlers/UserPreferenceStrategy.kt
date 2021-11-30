package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.models.Input
import com.workduck.service.UserPreferenceService

interface UserPreferenceStrategy {

    fun apply(input: Input, userPreferenceService: UserPreferenceService): ApiGatewayResponse
}