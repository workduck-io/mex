package com.serverless.userPreferenceHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.UserPreferenceHelper
import com.workduck.models.UserPreferenceRecord
import com.workduck.service.UserPreferenceService

class GetAllUserPreferencesForUserStrategy : UserPreferenceStrategy {
    override fun apply(
            input: Input,
            userPreferenceService: UserPreferenceService
    ): ApiGatewayResponse {
        val errorMessage = "Error getting user preferences"

        val userID = input.pathParameters?.id
        return if(userID != null) {
            val userPreferenceList: List<UserPreferenceRecord>? = userPreferenceService.getAllUserPreferencesForUser(userID)

            val userPreferenceResponseList = mutableListOf<Response?>()
            userPreferenceList?.map {
                userPreferenceResponseList.add(UserPreferenceHelper.convertUserPreferenceRecordToUserPreferenceResponse(it))
            }

            ApiResponseHelper.generateStandardResponse(userPreferenceList as Any?, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}