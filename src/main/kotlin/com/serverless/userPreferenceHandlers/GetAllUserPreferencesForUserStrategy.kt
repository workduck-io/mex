package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.UserPreferenceRecord
import com.workduck.service.UserPreferenceService

class GetAllUserPreferencesForUserStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Map<String, Any>,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting user preferences"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val id = pathParameters!!["id"] as String

        val userPreferenceList: List<UserPreferenceRecord>? = userPreferenceService.getAllUserPreferencesForUser(id)
        return ApiResponseHelper.generateStandardResponse(userPreferenceList as Any?, errorMessage)
    }
}