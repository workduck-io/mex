package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.utils.Messages
import com.serverless.utils.NodeHelper
import com.serverless.utils.withNotFoundException
import com.workduck.models.Entity
import com.workduck.service.NodeService

class GetNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val pathParameters = input.pathParameters
        val queryStringParameters = input.queryStringParameters
        val nodeID = pathParameters?.id as String

        val starredInfo = queryStringParameters?.let{
            it["starredInfo"].toBoolean()
        } ?: false


        val node: Entity = nodeService.getNode(nodeID, input.headers.workspaceID, input.tokenBody.userID, starredInfo = starredInfo).withNotFoundException()

        val nodeResponse : Response? = NodeHelper.convertNodeToNodeResponse(node)
        return ApiResponseHelper.generateStandardResponse(nodeResponse, Messages.ERROR_GETTING_NODE)
    }
}
