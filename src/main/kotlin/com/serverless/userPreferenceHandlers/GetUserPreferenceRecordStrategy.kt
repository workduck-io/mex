package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.UserPreferenceRecord
import com.workduck.service.UserPreferenceService

class GetUserPreferenceRecordStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Map<String, Any>,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting user records"

        val pathParameters = input["pathParameters"] as Map<*, *>?

        val userID = pathParameters!!["id"] as String
        val preferenceType = pathParameters["preferenceType"] as String

        val record: UserPreferenceRecord? = userPreferenceService.getUserPreferenceRecord(userID, preferenceType)
        return ApiResponseHelper.generateStandardResponse(record as Any?, errorMessage)
    }
}