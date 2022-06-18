package com.serverless.utils

import com.amazonaws.services.cognitoidp.model.UnauthorizedException
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
import com.amazonaws.services.dynamodbv2.model.ItemCollectionSizeLimitExceededException
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.exceptions.WDNodeSizeLargeException
import com.workduck.models.exceptions.WDNotFoundException


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
            is IllegalArgumentException -> ApiResponseHelper.generateStandard4xxErrorResponse(e,Messages.BAD_REQUEST, 400)
            is ClassCastException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.BAD_REQUEST, 400)
            is ConditionalCheckFailedException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.BAD_REQUEST, 400)
            is DynamoDBMappingException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.BAD_REQUEST, 400)
            is NoSuchElementException -> ApiResponseHelper.generateStandard4xxErrorResponse(e,Messages.RESOURCE_NOT_FOUND, 404)
            is ResourceNotFoundException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.RESOURCE_NOT_FOUND, 404)
            is WDNotFoundException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.RESOURCE_NOT_FOUND, 404)
            is NullPointerException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.NPE, 400)
            is UnauthorizedException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.UNAUTHORIZED, 401)
            is WDNodeSizeLargeException -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.ITEM_SIZE_LARGE, 400)
            is AmazonDynamoDBException ->  {
                when(e.errorMessage){
                    Messages.ITEM_SIZE_LARGE -> ApiResponseHelper.generateStandard4xxErrorResponse(e, Messages.ITEM_SIZE_LARGE, 400)
                    else -> ApiResponseHelper.generateStandard5xxErrorResponse(e,Messages.INTERNAL_SERVER_ERROR, 500)
                }
            }
            else -> ApiResponseHelper.generateStandard5xxErrorResponse(e,Messages.INTERNAL_SERVER_ERROR, 500)
        }
    }

}

