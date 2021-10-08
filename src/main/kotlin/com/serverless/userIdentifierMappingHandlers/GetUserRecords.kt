package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager
import java.util.*

class GetUserRecords: RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userIdentifierMappingService = UserIdentifierMappingService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {

		val pathParameters = input["pathParameters"] as Map<*, *>?
		val userID = pathParameters!!["userID"] as String


		val userRecords = userIdentifierMappingService.getUserRecords(userID)

		if (userRecords != null) {
			val responseBody = StandardResponse(userRecords.toString())
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error getting userRecords!")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetUserRecords::class.java)
	}
}