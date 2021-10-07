package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager
import java.util.*

class DeleteUserIdentifierMappingRecord : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userIdentifierMappingService = UserIdentifierMappingService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {

		val pathParameters = input["pathParameters"] as Map<*, *>?
		val userID = pathParameters!!["userID"] as String
		val identifierID = pathParameters!!["identifierID"] as String

		userIdentifierMappingService.deleteUserIdentifierMapping(userID, identifierID)

		val responseBody = Response("Go!!!!!! Serverless v1.x! Your Kotlin function executed successfully!", input)
		return ApiGatewayResponse.build {
			statusCode = 200
			objectBody = responseBody
			headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(DeleteUserIdentifierMappingRecord::class.java)
	}
}