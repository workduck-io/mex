package com.serverless

import java.util.*

object ApiResponseHelper {

    fun generateStandardResponse(passedObject: Any?, errorMessage: String): ApiGatewayResponse {

        if (passedObject != null) {
            return ApiGatewayResponse.build {
                statusCode = 200
                objectBody = passedObject
            }
        } else {
            val responseBody = StandardResponse(errorMessage)
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
                headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
            }
        }
    }

    fun generateResponseWithJsonList(passedObject: Any?, errorMessage: String): ApiGatewayResponse {

        if (passedObject != null) {
            return ApiGatewayResponse.buildWithJsonList {
                statusCode = 200
                objectBody = passedObject
            }
        } else {
            val responseBody = StandardResponse(errorMessage)
            return ApiGatewayResponse.build {
                statusCode = 500
                objectBody = responseBody
                headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
            }
        }
    }
}
