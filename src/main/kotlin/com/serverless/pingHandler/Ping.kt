package com.serverless.pingHandler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.StandardResponse
import org.apache.logging.log4j.LogManager

class Ping : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

	override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

		LOG.info("received: " + input.keys.toString())

		val responseBody = StandardResponse("Go Serverless v1.x! Your Kotlin function executed successfully!")
		return ApiGatewayResponse.build {
			statusCode = 200
			objectBody = responseBody
		}
	}

	companion object {
		private val LOG = LogManager.getLogger(Ping::class.java)
	}
}
