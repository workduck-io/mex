package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.Response
import com.serverless.models.WDRequest
import com.serverless.transformers.Transformer
import com.serverless.utils.NodeHelper
import com.workduck.models.Entity
import com.workduck.models.Node
import com.workduck.service.NodeService

class CreateNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error creating node"

        val nodeRequest : WDRequest? = input.payload
        val node: Entity? = nodeService.createAndUpdateNode(nodeRequest)

        val nodeResponse: Response? = NodeHelper.convertNodeToNodeResponse(node)
        return ApiResponseHelper.generateStandardResponse(nodeResponse, errorMessage)
    }
}
