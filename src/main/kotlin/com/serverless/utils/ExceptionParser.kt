package com.serverless.utils

import com.amazonaws.services.kms.model.InvalidArnException
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import jdk.internal.util.Preconditions


/**
 * Application isa combination of standard errors plus customised behaviour
 *
 * We should always try to fit into standard errors
 *
 * Every standardized error should effectively map to standard http response code
 *
 * IllegalArgumentException -> 400
 * IllegalAccessException -> 404
 *
 * //wrong behaviour
 * class CE : Exception{}
 *
 * // if really needed do this
 * class CE : IllegalArgumentException{}
 *
 */
object ExceptionParser {
    fun exceptionHandler(e: Exception) : ApiGatewayResponse{
        return when(e){
            is IllegalArgumentException -> ApiResponseHelper.generateStandardErrorResponse(e.message?:"Error performing action", 400)
            else -> {
                throw e
            }

        }
    }
}

