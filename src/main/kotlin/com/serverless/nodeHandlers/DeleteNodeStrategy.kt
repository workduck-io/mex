package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.transformers.Transformer
import com.serverless.utils.IdentifierHelper
import com.workduck.models.Identifier
import com.workduck.models.Node
import com.workduck.service.NodeService

class DeleteNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error deleting node"

        val nodeID = input.pathParameters?.id
        val bodyJson = input["body"] as String
        return if (nodeID != null) {
            val identifier: Identifier? = nodeService.deleteNode(nodeID)

            val identifierResponse = IdentifierHelper.convertIdentifierToIdentifierResponse(identifier)
            ApiResponseHelper.generateStandardResponse(identifierResponse, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
