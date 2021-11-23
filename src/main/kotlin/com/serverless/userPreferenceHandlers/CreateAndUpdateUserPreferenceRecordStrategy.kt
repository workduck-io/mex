package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.UserPreferenceRecord
import com.workduck.service.UserPreferenceService

class CreateAndUpdateUserPreferenceRecordStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Map<String, Any>,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val errorMessage = "Error updating preferences"

        val json = input["body"] as String
        val record: UserPreferenceRecord? = userPreferenceService.createAndUpdateUserPreferenceRecord(json)
        return ApiResponseHelper.generateStandardResponse(record as Any?, errorMessage)
    }
}