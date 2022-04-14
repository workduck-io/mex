package com.serverless.utils

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
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
        println("in the exception parser")
        return when(e){
            is IllegalArgumentException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Error performing action", 400)
            is NoSuchElementException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Not Found", 404)
            is NullPointerException -> ApiResponseHelper.generateStandardErrorResponse(e.message ?: "Getting NPE", 400)
            is UnauthorizedException ->{
                LOG.info("Unauthorized")
                ApiResponseHelper.generateStandardErrorResponse(e.message ?: "Unauthorized", 401)
            }
            is ResourceNotFoundException -> {
                LOG.warn(e)
                ApiResponseHelper.generateStandardErrorResponse("Internal Server Error", 404)
            }
            is ClassCastException -> ApiResponseHelper.generateStandardErrorResponse("Malformed Request", 400)
            is ConditionalCheckFailedException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Error performing action", 500)
            is AmazonDynamoDBException -> ApiResponseHelper.generateStandardErrorResponse(e.message?: "Error performing action", 500)
            is DynamoDBMappingException -> ApiResponseHelper.generateStandardErrorResponse("Internal Server Error", 500)
            else -> {
                throw e
            }

        }
    }





}

