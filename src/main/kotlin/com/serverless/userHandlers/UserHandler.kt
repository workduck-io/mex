package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.StandardResponse
import com.workduck.models.Entity
import com.workduck.models.Identifier
import com.workduck.service.UserService
import org.apache.logging.log4j.LogManager

class UserHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userService = UserService()

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		val method = input["httpMethod"] as String
		val path = input["path"] as String

		if (method == "GET" && path.startsWith("/user/USER")) {

			val errorMessage = "Error getting user"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val userID = pathParameters!!["id"] as String

			val user: Entity? = userService.getUser(userID)
			return ApiResponseHelper.generateStandardResponse(user as Any?, errorMessage)

		} else if (method == "POST" && path == "/user") {

			val errorMessage = "Error creating user"

			val json = input["body"] as String
			val user: Entity? = userService.createUser(json)
			return ApiResponseHelper.generateStandardResponse(user as Any?, errorMessage)

		} else if (method == "DELETE" && path.startsWith("/user/USER")) {

			val errorMessage = "Error deleting user"

			val pathParameters = input["pathParameters"] as Map<*, *>?
			val userID = pathParameters!!["id"] as String

			val identifier: Identifier? = userService.deleteUser(userID)
			return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)

		} else if (method == "POST" && path == "/user/update") {
			val errorMessage = "Error updating user"
			val json = input["body"] as String

			val user: Entity? = userService.updateUser(json)
			return ApiResponseHelper.generateStandardResponse(user as Any?, errorMessage)

		} else if (method == "GET" && path.startsWith("/user/namespace/NAMESPACE")) {
			val errorMessage = "Error getting users!"
			val pathParameters = input["pathParameters"] as Map<*, *>?
			val namespaceID = pathParameters!!["id"] as String

			val users: MutableList<String>? = userService.getAllUsersWithNamespaceID(namespaceID)

			return ApiResponseHelper.generateStandardResponse(users as Any?, errorMessage)

		} else if (method == "GET" && path.startsWith("/user/workspace/WORKSPACE")) {
			val errorMessage = "Error getting users!"
			val pathParameters = input["pathParameters"] as Map<*, *>?
			val workspaceID = pathParameters!!["id"] as String

			val users: MutableList<String>? = userService.getAllUsersWithWorkspaceID(workspaceID)
			return ApiResponseHelper.generateStandardResponse(users as Any?, errorMessage)

		} else {
			val responseBody = StandardResponse("Request type not recognized")
			return ApiGatewayResponse.build {
				statusCode = 500
				objectBody = responseBody
			}
		}

	}

	companion object {
		private val LOG = LogManager.getLogger(UserHandler::class.java)
	}
}