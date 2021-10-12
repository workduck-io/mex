package com.serverless.userIdentifierMappingHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.service.UserIdentifierMappingService
import org.apache.logging.log4j.LogManager

class UserIdentifierMappingHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userIdentifierMappingService = UserIdentifierMappingService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val path = input["path"] as String

		if (method == "POST" && path == "/userIdentifierMappingRecord") {

			val errorMessage = "Error creating userIdentifierMapping"

			val json = input["body"] as String

			val userIdentifierMapping : Entity? = userIdentifierMappingService.createUserIdentifierRecord(json)

			return ApiResponseHelper.generateStandardResponse(userIdentifierMapping as Any?, errorMessage)

		} else if (method == "DELETE" && path.startsWith("/userIdentifierMappingRecord/")) {

			val errorMessage = "Error deleting userIdentifierMapping"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val userID = pathParameters!!["userID"] as String
			val identifierID = pathParameters!!["identifierID"] as String

			val map : Map<String, String>? = userIdentifierMappingService.deleteUserIdentifierMapping(userID, identifierID)
			return ApiResponseHelper.generateStandardResponse(map as Any?, errorMessage)

		} else if (method == "GET" && path.startsWith( "/userRecords/") ){
			val errorMessage = "Error getting user records"
			val pathParameters = input["pathParameters"] as Map<*, *>?
			val userID = pathParameters!!["userID"] as String


			val userRecords : MutableList<String>? = userIdentifierMappingService.getUserRecords(userID)
			return ApiResponseHelper.generateResponseWithJsonList(userRecords as Any?, errorMessage)

		} else {
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}
		}

	}

	companion object {
		private val LOG = LogManager.getLogger(UserIdentifierMappingHandler::class.java)
	}
}