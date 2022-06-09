package com.serverless.utils

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.exceptions.WDNotFoundException
import org.apache.logging.log4j.LogManager


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

    //TODO(should be combine all DDB Exceptions together?)
    fun exceptionHandler(e: Exception) : ApiGatewayResponse{
        return when(e){
            is IllegalArgumentException -> ApiResponseHelper.generateStandard4xxErrorResponse(e,"Error performing action", 400)
            is NoSuchElementException -> ApiResponseHelper.generateStandard4xxErrorResponse(e,"Not Found", 404)
            is NullPointerException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Getting NPE", 400)
            is UnauthorizedException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Unauthorized", 401)
            is ResourceNotFoundException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Internal Server Error", 404)
            is ClassCastException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Malformed Request", 400)
            is ConditionalCheckFailedException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Bad Request", 400)
            is DynamoDBMappingException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Bad Request", 400)
            is WDNotFoundException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, "Requested resource not found", 404)
            else -> {
                ApiResponseHelper.generateStandard5xxErrorResponse(e,"Internal Server Error", 500)
            }
        }
    }

}

