package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Node
import com.workduck.service.NodeService

class GetNodeStrategy(
        val nodeTransformer : Transformer<Node>
) : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error getting node"

        println(input)
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

        val node: Entity? = nodeService.getNode(nodeID, bookmarkInfo, userID)

        val nodeResponse : Response? = nodeTransformer.transform(node as Node?)
        return ApiResponseHelper.generateStandardResponse(nodeResponse, errorMessage)
    }
}
