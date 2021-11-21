package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.service.NodeService

class MakeNodePublicStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error making node public"

        val pathParameters = input["pathParameters"] as Map<*, *>?
        val nodeID = pathParameters!!["id"] as String

        val identifier: String? = nodeService.makeNodePublic(nodeID)
        return ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
    }
}
