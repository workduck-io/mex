package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class GetNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting node"

        println(input)
        val pathParameters = input["pathParameters"] as Map<*, *>?
        val queryStringParameters = input["queryStringParameters"] as Map<String, String>?
        val nodeID = pathParameters!!["id"] as String

        val bookmarkInfo = queryStringParameters?.let{
            it["bookmarkInfo"].toBoolean()
        }

        val userID = queryStringParameters?.let{
            it["userID"]
        }

        val node: Entity? = nodeService.getNode(nodeID, bookmarkInfo, userID)
        return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)
    }
}
