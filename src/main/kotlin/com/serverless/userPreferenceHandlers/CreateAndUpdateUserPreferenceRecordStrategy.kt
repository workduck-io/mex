package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.requests.WDRequest
import com.serverless.utils.Messages
import com.workduck.service.UserPreferenceService

class CreateAndUpdateUserPreferenceRecordStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Input,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val userPreferenceRequest : WDRequest? = input.payload

        return if(userPreferenceRequest != null) {
            userPreferenceService.createAndUpdateUserPreferenceRecord(userPreferenceRequest)

            //val userPreferenceResponse = UserPreferenceHelper.convertUserPreferenceRecordToUserPreferenceResponse(record)
            ApiResponseHelper.generateStandardResponse(null,  201, Messages.ERROR_UPDATING_RECORDS)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(Messages.ERROR_UPDATING_RECORDS)
        }
    }
}