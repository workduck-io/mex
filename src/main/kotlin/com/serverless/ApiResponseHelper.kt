package com.serverless

import com.serverless.models.responses.Response
import org.apache.logging.log4j.LogManager
import java.lang.Exception
import java.util.Collections

object ApiResponseHelper {

    private val LOG = LogManager.getLogger(ApiResponseHelper::class.java)

    fun generateStandardResponse(passedObject: Any? = null, errorMessage: String): ApiGatewayResponse {

        return if (passedObject != null) {
            ApiGatewayResponse.build {
                statusCode = 200
                objectBody = passedObject
            }
        } else {
            generateStandardErrorResponse(errorMessage)
        }
    }


    fun generateStandardResponse(passedObject: Response?, statusCodePassed: Int = 200, errorMessage: String): ApiGatewayResponse {

        return ApiGatewayResponse.build {
            statusCode = statusCodePassed
            objectBody = passedObject
        }
    }

    fun generateResponseWithJsonList(passedObject: Any?, errorMessage: String): ApiGatewayResponse {

        return if (passedObject != null) {
            ApiGatewayResponse.buildWithJsonList {
                statusCode = 200
                objectBody = passedObject
            }
        } else {
            generateStandardErrorResponse(errorMessage)
        }
    }

    fun generateStandardErrorResponse(errorMessage: String): ApiGatewayResponse {
        LOG.warn(errorMessage)
        val responseBody = StandardResponse(errorMessage)
        return ApiGatewayResponse.build {
            statusCode = 400
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }

    fun generateStandard5xxErrorResponse(
            exception: Exception,
            defaultMessage: String,
            errorCode: Int
    ): ApiGatewayResponse {
        LOG.error(exception.stackTraceToString())
        val responseBody = StandardResponse(exception.message ?: defaultMessage)
        return ApiGatewayResponse.build {
            statusCode = errorCode
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }


    fun generateStandard4xxErrorResponse(
            exception: Exception,
            defaultMessage: String,
            errorCode: Int
    ): ApiGatewayResponse {
        LOG.warn(exception.stackTraceToString())
        val responseBody = StandardResponse(exception.message ?: defaultMessage)
        return ApiGatewayResponse.build {
            statusCode = errorCode
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }


    fun generateStandardErrorResponse(
        errorMessage: String,
        errorCode: Int
    ): ApiGatewayResponse {
        LOG.warn(errorMessage)
        val responseBody = StandardResponse(errorMessage)
        return ApiGatewayResponse.build {
            statusCode = errorCode
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }
}
