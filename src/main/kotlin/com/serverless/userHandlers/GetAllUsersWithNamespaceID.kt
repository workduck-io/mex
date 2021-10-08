package com.serverless.userHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.workduck.service.NodeService
import com.workduck.service.UserService
import org.apache.logging.log4j.LogManager
import java.util.*

class GetAllUsersWithNamespaceID: RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	private val userService = UserService()

	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?
		val namespaceID = pathParameters!!["id"] as String

		println(userService.getAllUsersWithNamespaceID(namespaceID))
		LOG.info("received: " + input.keys.toString())

		val responseBody = Response("Go!!!!!! Serverless v1.x! Your Kotlin function executed successfully!", input)
		return ApiGatewayResponse.build {
			statusCode = 200
			objectBody = responseBody
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetAllUsersWithNamespaceID::class.java)
	}
}