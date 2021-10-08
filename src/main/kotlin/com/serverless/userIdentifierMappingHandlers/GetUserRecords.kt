package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager
import java.util.*

class GetUserRecords: RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userIdentifierMappingService = UserIdentifierMappingService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {

		val pathParameters = input["pathParameters"] as Map<*, *>?
		val userID = pathParameters!!["userID"] as String


		userIdentifierMappingService.getUserRecords(userID)

		val responseBody = Response("Go!!!!!! Serverless v1.x! Your Kotlin function executed successfully!", input)
		return ApiGatewayResponse.build {
			statusCode = 200
			objectBody = responseBody
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetUserRecords::class.java)
	}
}