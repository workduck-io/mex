package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.serverless.StandardResponse
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager
import java.util.*

class CreateUserIdentifierMappingRecord: RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userIdentifierMappingService = UserIdentifierMappingService()
	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {

		val json = input["body"] as String

		val userIdentifierRecord = userIdentifierMappingService.createUserIdentifierRecord(json)

		if (userIdentifierRecord != null) {
			val responseBody = StandardResponse(userIdentifierRecord.toString())
			return ApiGatewayResponse.build {
				statusCode = 200
				objectBody = responseBody
			}
		}
		else{
			val responseBody = StandardResponse("Error creating userIdentifierRecord")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
				headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
			}
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(CreateUserIdentifierMappingRecord::class.java)
	}
}