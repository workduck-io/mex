package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class CreateNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error creating node"

        val json = input["body"] as String
        val node: Entity? = nodeService.createAndUpdateNode(json)
        return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)
    }
}
