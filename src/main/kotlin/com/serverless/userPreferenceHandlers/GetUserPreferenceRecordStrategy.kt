package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.UserPreferenceHelper
import com.workduck.models.UserPreferenceRecord
import com.workduck.service.UserPreferenceService

class GetUserPreferenceRecordStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Input,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting user records"

        val userID = input.pathParameters?.id
        val preferenceType = input.pathParameters?.preferenceType

        return if(userID != null && preferenceType != null) {
            val record: UserPreferenceRecord? = userPreferenceService.getUserPreferenceRecord(userID, preferenceType)

            val userPreferenceResponse = UserPreferenceHelper.convertUserPreferenceRecordToUserPreferenceResponse(record)
            ApiResponseHelper.generateStandardResponse(userPreferenceResponse, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}