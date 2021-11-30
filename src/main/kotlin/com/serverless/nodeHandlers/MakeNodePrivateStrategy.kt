package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.utils.IdentifierHelper
import com.sun.tools.javac.util.DefinedBy
import com.workduck.service.NodeService

class MakeNodePrivateStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error making node private"

        val nodeID = input.pathParameters?.id

        return if(nodeID != null) {

            val returnedID: String? = nodeService.makeNodePrivate(nodeID)

            ApiResponseHelper.generateStandardResponse(returnedID, errorMessage)
        }
        else{
            ApiResponseHelper.generateStandardErrorResponse(errorMessage)
        }
    }
}
