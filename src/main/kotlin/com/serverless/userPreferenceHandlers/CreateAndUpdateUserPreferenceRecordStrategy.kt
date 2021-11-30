package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.UserPreferenceRequest
import com.serverless.models.WDRequest
import com.serverless.utils.UserPreferenceHelper
import com.workduck.models.UserPreferenceRecord
import com.workduck.service.UserPreferenceService

class CreateAndUpdateUserPreferenceRecordStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Input,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val errorMessage = "Error updating preferences"

        val userPreferenceRequest : WDRequest? = input.payload

        return if(userPreferenceRequest != null) {
            val record: UserPreferenceRecord? = userPreferenceService.createAndUpdateUserPreferenceRecord(userPreferenceRequest)

            val userPreferenceResponse = UserPreferenceHelper.convertUserPreferenceRecordToUserPreferenceResponse(record)
            ApiResponseHelper.generateStandardResponse(userPreferenceResponse, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}