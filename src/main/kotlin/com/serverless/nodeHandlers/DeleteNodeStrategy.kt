package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Node
import com.workduck.service.NodeService

class DeleteNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService, transformer: Transformer<Node>): ApiGatewayResponse {
        val errorMessage = "Error deleting node"

        val pathParameters = input["pathParameters"] as Map<String, String>?

        return if (pathParameters != null) {
            val nodeID = pathParameters.getOrDefault("id", "")

            val identifier: Identifier? = nodeService.deleteNode(nodeID)
            ApiResponseHelper.generateStandardResponse(identifier as Any?, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
