package com.serverless.namespaceHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.workduck.service.NamespaceService
import org.apache.logging.log4j.LogManager
import java.util.*

class GetNamespaceData : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
	override fun handleRequest(input:Map<String, Any>, context: Context): ApiGatewayResponse {


		val pathParameters = input["pathParameters"] as Map<*, *>?

		val namespaceIDList: List<String> = (pathParameters!!["ids"] as String).split(",")
		println(NamespaceService().getNamespaceData(namespaceIDList))


		val responseBody = Response("Go!!!!!! Serverless v1.x! Your Kotlin function executed successfully!", input)
		return ApiGatewayResponse.build {
			statusCode = 200
			objectBody = responseBody
			headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
		}
	}
	companion object {
		private val LOG = LogManager.getLogger(GetNamespaceData::class.java)
	}
}