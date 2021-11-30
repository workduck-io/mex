package com.serverless.utils

import com.amazonaws.services.kms.model.InvalidArnException
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper

object ExceptionParser {

    fun exceptionHandler(e: Exception) : ApiGatewayResponse{
        return when(e){
            is IllegalArgumentException -> {
                ApiResponseHelper.generateStandardErrorResponse(e.message?:"Error performing action")
            }

            else -> {
                throw e
            }

        }
    }
}