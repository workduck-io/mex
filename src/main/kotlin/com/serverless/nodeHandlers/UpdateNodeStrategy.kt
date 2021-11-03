package com.serverless.nodeHandlers

import com.serverless.ApiGatewayResponse
import com.serverless.ApiResponseHelper
import com.workduck.models.Entity
import com.workduck.service.NodeService

class UpdateNodeStrategy : NodeStrategy {
    override fun apply(input: Map<String, Any>, nodeService: NodeService): ApiGatewayResponse {
        val errorMessage = "Error updating node"
        val json = input["body"] as String

        val node: Entity? = nodeService.updateNode(json)
        return ApiResponseHelper.generateStandardResponse(node as Any?, errorMessage)
    }
}
