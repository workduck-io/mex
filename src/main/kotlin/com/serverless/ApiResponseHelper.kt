package com.serverless

import com.serverless.models.Response
import java.util.Collections

object ApiResponseHelper {

    fun generateStandardResponse(passedObject: Any?, errorMessage: String): ApiGatewayResponse {

        return if (passedObject != null) {
            ApiGatewayResponse.build {
                statusCode = 200
                objectBody = passedObject
            }
        } else {
            generateStandardErrorResponse(errorMessage)
        }
    }


    fun generateStandardResponse(passedObject: Response?, errorMessage: String): ApiGatewayResponse {

        return if (passedObject != null) {
            ApiGatewayResponse.build {
                statusCode = 200
                objectBody = passedObject
            }
        } else {
            generateStandardErrorResponse(errorMessage)
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
            statusCode = 500
            objectBody = responseBody
            headers = Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless")
        }
    }
}
