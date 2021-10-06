package com.serverless.nodeHandlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.ApiGatewayResponse
import com.serverless.Response
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*

class AppendDataToNode:RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    override fun handleRequest(input:Map<String, Any>, context:Context): ApiGatewayResponse {

        val json = input["body"] as String
        println("BODY STARTS" + json + "BODY ENDS")

		val pathParameters = input["pathParameters"] as Map<*, *>?
		val nodeID = pathParameters!!["id"] as String
		println("NODE ID : $nodeID")

		NodeService().append(nodeID, json)

        LOG.info("received: " + input.keys.toString())

        val responseBody = Response("Go!!!!!! Serverless v1.x! Your Kotlin function executed successfully!", input)
        return ApiGatewayResponse.build {
			statusCode = 200
			objectBody = responseBody
			headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
		}
    }
    companion object {
        private val LOG = LogManager.getLogger(AppendDataToNode::class.java)
    }
}