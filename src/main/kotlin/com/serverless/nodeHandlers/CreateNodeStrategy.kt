package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.WDRequest
import com.serverless.transformers.Transformer
import com.workduck.models.Entity
import com.workduck.models.Node
import com.workduck.service.NodeService

class CreateNodeStrategy(
        val nodeTransformer : Transformer<Node>
) : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error creating node"

        val nodeRequest : WDRequest? = input.payload
        val node: Entity? = nodeService.createAndUpdateNode(nodeRequest)
        return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)
    }
}
