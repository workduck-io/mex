package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.workduck.service.NamespaceService
import com.workduck.service.NodeService
import org.apache.logging.log4j.LogManager
import java.util.*

class UpdateNode:RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    override fun handleRequest(input:Map<String, Any>, context:Context):ApiGatewayResponse {

        val json = input["body"] as String
        println("BODY STARTS" + json + "BODY ENDS")
        println(NodeService().updateNode(json))
        LOG.info("received: " + input.keys.toString())

        val responseBody = Response("Go!!!!!! Serverless v1.x! Your Kotlin function executed successfully!", input)
        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = responseBody
            headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
        }
    }
    companion object {
        private val LOG = LogManager.getLogger(UpdateNode::class.java)
    }
}