package com.serverless

import com.serverless.models.responses.Response
import java.util.Collections

object ApiResponseHelper {

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
        val responseBody = StandardResponse(errorMessage)
        return ApiGatewayResponse.build {
            statusCode = 400
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }

    fun generateStandardErrorResponse(
        errorMessage: String,
        errorCode: Int
    ): ApiGatewayResponse {
        val responseBody = StandardResponse(errorMessage)
        return ApiGatewayResponse.build {
            statusCode = errorCode
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }
}
