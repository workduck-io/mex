package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class GetNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val pathParameters = input.pathParameters
        val queryStringParameters = input.queryStringParameters
        println("pathParameters : $pathParameters")
        println("queryParameters : $queryStringParameters")
        val nodeID = pathParameters?.id as String

        val bookmarkInfo = queryStringParameters?.let{
            it["bookmarkInfo"].toBoolean()
        }

        val userID = queryStringParameters?.let{
            it["userID"]
        }

        val node: Entity? = nodeService.getNode(nodeID, input.headers.workspaceID, bookmarkInfo, userID)

        val nodeResponse : Response? = NodeHelper.convertNodeToNodeResponse(node)
        return ApiResponseHelper.generateStandardResponse(nodeResponse, Messages.ERROR_GETTING_NODE)
    }
}
