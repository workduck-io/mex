package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.serverless.models.Input
import com.serverless.models.responses.Response
import com.serverless.models.requests.WDRequest
import com.serverless.utils.NodeHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class CreateNodeStrategy : NodeStrategy {
    override fun apply(input: Input, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error creating node"

        val nodeRequest : WDRequest? = input.payload
        val versionEnabled : Boolean? = input.queryStringParameters?.let{
            it["versionEnabled"].toBoolean()
        }

        val node: Entity? = if(versionEnabled != null)
            nodeService.createAndUpdateNode(nodeRequest, input.headers.workspaceID, versionEnabled)
        else nodeService.createAndUpdateNode(nodeRequest, input.headers.workspaceID)

        val nodeResponse: Response? = NodeHelper.convertNodeToNodeResponse(node)
        return ApiResponseHelper.generateStandardResponse(nodeResponse, errorMessage)
    }
}
