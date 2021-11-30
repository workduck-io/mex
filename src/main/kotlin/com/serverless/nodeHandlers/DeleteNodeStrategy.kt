package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.workduck.service.NodeService

class DeleteNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error deleting node"

        val nodeIDRequest = input.payload

        return if (nodeIDRequest != null) {

            val deleteNodeIDList: MutableList<String>? = nodeService.deleteNodes(nodeIDRequest)

            //val identifierResponse = IdentifierHelper.convertIdentifierToIdentifierResponse(identifier)
            ApiResponseHelper.generateStandardResponse(deleteNodeIDList, errorMessage)
        } else {
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
