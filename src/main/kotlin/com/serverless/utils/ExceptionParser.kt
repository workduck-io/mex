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

    private val LOG = LogManager.getLogger(ExceptionParser::class.java)

    //TODO(should be combine all DDB Exceptions together?)
    fun exceptionHandler(e: Exception) : ApiGatewayResponse{
        logStackTrace(e)
        return when(e){
            is IllegalArgumentException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Error performing action", 400)
            is NoSuchElementException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Not Found", 404)
            is NullPointerException -> ApiResponseHelper.generateStandardErrorResponse(e.message ?: "Getting NPE", 400)
            is UnauthorizedException -> ApiResponseHelper.generateStandardErrorResponse(e.message ?: "Unauthorized", 401)
            is ResourceNotFoundException -> ApiResponseHelper.generateStandardErrorResponse("Internal Server Error", 404)
            is ClassCastException -> ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)
            is ConditionalCheckFailedException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Error performing action", 500)
            is AmazonDynamoDBException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Error performing action", 500)
            is DynamoDBMappingException -> ApiResponseHelper.generateStandardErrorResponse("Bad Input", 400)
            is WDNotFoundException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Requested resource not found", 404)
            else -> {
                ApiResponseHelper.generateStandardErrorResponse("Internal Server Error", 500)
            }
        }
    }

    private fun logStackTrace(exception : Exception){
        LOG.error(exception.stackTraceToString())
    }





}

