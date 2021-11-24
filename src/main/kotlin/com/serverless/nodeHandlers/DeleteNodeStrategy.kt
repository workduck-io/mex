package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.workduck.models.Identifier
import com.workduck.models.Node
import com.workduck.service.NodeService

class DeleteNodeStrategy(
        val identifierTransformer : Transformer<Identifier>
) : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error deleting node"

        val nodeID = input.pathParameters?.id

        return if (nodeID != null) {
            val identifier: Identifier? = nodeService.deleteNode(nodeID)

            val identifierResponse = identifierTransformer.transform(identifier)
            ApiResponseHelper.generateStandardResponse(identifierResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
